DROP TABLE IF EXISTS desk_assignment CASCADE;
DROP TABLE IF EXISTS waitlist_offer CASCADE;
DROP TABLE IF EXISTS waitlist_entry CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS resource_features CASCADE;
DROP TABLE IF EXISTS resource CASCADE;
DROP TABLE IF EXISTS floor CASCADE;
DROP TABLE IF EXISTS building CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- ===============================
-- MASTER TABLES
-- ===============================

CREATE TABLE role (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE department (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL
);

CREATE TABLE building (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL
);

CREATE TABLE floor (
                       id SERIAL PRIMARY KEY,
                       building_id INTEGER REFERENCES building(id) ON DELETE CASCADE,
                       name VARCHAR(100) NOT NULL
);

CREATE TABLE resource (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          type VARCHAR(20) NOT NULL,
                          department_id INTEGER REFERENCES department(id) ON DELETE SET NULL,
                          floor_id INTEGER REFERENCES floor(id) ON DELETE CASCADE,
                          capacity INTEGER NOT NULL,
                          desk_mode VARCHAR(20) NOT NULL
);

CREATE TABLE resource_features (
                                   resource_id INTEGER NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
                                   feature VARCHAR(255) NOT NULL
);

CREATE TABLE user_account (
                              id SERIAL PRIMARY KEY,
                              username VARCHAR(100) UNIQUE NOT NULL,
                              password VARCHAR(255) NOT NULL,
                              role_id INTEGER REFERENCES role(id),
                              department_id INTEGER REFERENCES department(id),
                              email VARCHAR(100) UNIQUE NOT NULL
);

-- ===============================
-- BOOKING (ALIGNED WITH ENTITY)
-- ===============================

CREATE TABLE booking (
                         id SERIAL PRIMARY KEY,
                         resource_id INTEGER NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
                         user_id INTEGER NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
                         start_utc TIMESTAMP NOT NULL,
                         end_utc TIMESTAMP NOT NULL,
                         status VARCHAR(30) NOT NULL,
                         recurrence_group_id UUID
);

CREATE INDEX idx_booking_resource_time
    ON booking(resource_id, start_utc, end_utc);

CREATE INDEX idx_booking_recurrence_group
    ON booking(recurrence_group_id);

-- ===============================
-- WAITLIST ENTRY
-- ===============================

CREATE TABLE waitlist_entry (
                                id SERIAL PRIMARY KEY,
                                resource_id INTEGER NOT NULL REFERENCES resource(id) ON DELETE CASCADE,
                                start_utc TIMESTAMP NOT NULL,
                                end_utc TIMESTAMP NOT NULL,
                                user_id INTEGER NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
                                created_at_utc TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uq_waitlist_unique_user_slot
    ON waitlist_entry(resource_id, start_utc, end_utc, user_id);

CREATE INDEX idx_waitlist_fifo
    ON waitlist_entry(resource_id, start_utc, end_utc, created_at_utc);

-- ===============================
-- WAITLIST OFFER (UPDATED)
-- ===============================

CREATE TABLE waitlist_offer (
                                id SERIAL PRIMARY KEY,
                                waitlist_entry_id INTEGER NOT NULL REFERENCES waitlist_entry(id) ON DELETE CASCADE,
                                status VARCHAR(30) NOT NULL,
                                offered_at_utc TIMESTAMP NOT NULL,
                                expires_at_utc TIMESTAMP NOT NULL,
                                provisional_booking_id INTEGER UNIQUE REFERENCES booking(id) ON DELETE SET NULL
);

CREATE INDEX idx_offer_slot
    ON waitlist_offer(status, expires_at_utc);

-- ===============================
-- DESK ASSIGNMENT
-- ===============================

CREATE TABLE desk_assignment (
                                 id SERIAL PRIMARY KEY,
                                 resource_id INTEGER REFERENCES resource(id) ON DELETE CASCADE,
                                 user_id INTEGER REFERENCES user_account(id) ON DELETE CASCADE,
                                 start_utc TIMESTAMP NOT NULL,
                                 end_utc TIMESTAMP
);

-- ===============================
-- SEED DATA
-- ===============================

INSERT INTO role (name) VALUES
                            ('SYSTEM_ADMIN'),
                            ('DEPARTMENT_ADMIN'),
                            ('USER');

INSERT INTO department (name) VALUES
                                  ('Engineering'),
                                  ('HR'),
                                  ('Sales'),
                                  ('ADMIN');

INSERT INTO building (name) VALUES ('Main Office');

INSERT INTO floor (building_id, name)
VALUES (1, 'First Floor');

INSERT INTO resource (name, type, department_id, floor_id, capacity, desk_mode) VALUES
                                                                                    ('Room 101', 'ROOM', 1, 1, 10, 'HOT_DESK'),
                                                                                    ('Desk A1', 'DESK', 1, 1, 1, 'ASSIGNED'),
                                                                                    ('Parking P1', 'PARKING', 2, 1, 1, 'HOT_DESK');

INSERT INTO resource_features (resource_id, feature) VALUES
                                                         (1, 'Projector'),
                                                         (1, 'Whiteboard'),
                                                         (2, 'Projector'),
                                                         (2, 'Whiteboard'),
                                                         (3, 'Projector'),
                                                         (3, 'Whiteboard');

INSERT INTO user_account (username, password, role_id, department_id, email) VALUES
                                                                                 ('sysadmin', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 1, 4, 'sysadmin@vertexspace.com'),
                                                                                 ('deptadmin', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 2, 1, 'deptadmin@vertexspace.com'),
                                                                                 ('user1', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 3, 1, 'user1@vertexspace.com');

