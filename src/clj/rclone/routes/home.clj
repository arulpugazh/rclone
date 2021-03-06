(ns rclone.routes.home
  (:require [rclone.layout :as layout]
            [rclone.db.core :as db]
            [rclone.routes.dbinterface :refer :all]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia :refer [execute]]
            [clojure.edn :as edn]
            [clj-time.local :as local]
            [clojure.zip :as zip]))

(def resolvers (transient []))

(defn home-page []
  (layout/render "home.html"))

(defn wrap-resolver
  [f & args]
  (let [v (apply f args)]
    (cond
      (map? v) v
      (vector? v) v
      (nil? v) (response/not-found)
      :else (into {} v))))

(defn ^:private keyword-factory
  [keyword]
  (fn [context arguments value]
    (let [f @(resolve (symbol (name keyword)))]
      #(f context arguments value))))

(defn map-zipper [m]
  (zip/zipper 
   (fn [x] (or (map? x) (map? (nth x 1))))
   (fn [x] (seq (if (map? x) x (nth x 1))))
   (fn [x children] 
     (if (map? x) 
       (into {} children) 
       (assoc x 1 (into {} children))))
   m))

(defn get-keys-tree
  "Supply map zipper of tree and key to search"
  [tz]
  (if (not (zip/end? tz))
    (let [node (first tz)]
      (when (= (first node) :resolve)
        (conj! resolvers (second node)))
      (recur (zip/next tz)))
    (persistent! resolvers)))

(defn get-resolvers
  []
  (as-> (io/resource "edn/schema.edn") c
    (slurp c)
    (edn/read-string c)
    (map-zipper c)
    (get-keys-tree c)
    (map #(assoc {} % (partial wrap-resolver @(resolve (symbol (name %))))) c)
    (into {} c)))

(defn compile-schema
  []
  (let [resolvers (get-resolvers)]
    (as-> (io/resource "edn/schema.edn") c
      (slurp c)
      (edn/read-string c)
      (util/attach-resolvers c resolvers)
      (schema/compile c)
      )))

(def compiled-schema (compile-schema))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))
