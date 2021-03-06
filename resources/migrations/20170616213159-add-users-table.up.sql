CREATE TABLE users
(id VARCHAR(50) PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(50),
 admin BOOLEAN,
 created TIME,
 changed TIME,
 is_active BOOLEAN,
 pass VARCHAR(300));

CREATE TABLE groups
(id VARCHAR(50) PRIMARY KEY,
 description TEXT,
 rules TEXT,
 created TIME,
 changed TIME,
 created_by VARCHAR(50) REFERENCES users(id));

CREATE TABLE posts
(id BIGSERIAL PRIMARY KEY,
 title VARCHAR(30),
 url VARCHAR(2083),
 description TEXT,
 votes INTEGER,
 created TIME,
 changed TIME,
 posted_by VARCHAR(50) references users(id),
 posted_in VARCHAR(50) references groups(id));

CREATE TABLE comments
(id BIGSERIAL PRIMARY KEY,
 description TEXT,
 votes INTEGER,
 created TIME,
 changed TIME,
 posted_to BIGINT REFERENCES POSTS(id),
 replied_to BIGINT REFERENCES comments(id),
 commented_by VARCHAR(50) REFERENCES USERS(id));

CREATE TABLE subscriptions
(id BIGSERIAL PRIMARY KEY,
 subscribed_to VARCHAR(50) REFERENCES groups(id),
 subscriber VARCHAR(50) REFERENCES users(id),
 created TIME);

CREATE TABLE privileges
(id BIGSERIAL PRIMARY KEY,
 subscription BIGINT REFERENCES subscriptions(id),
 privilege VARCHAR(50),
 created TIME);
