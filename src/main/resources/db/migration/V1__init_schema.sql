
DROP TABLE IF EXISTS desk_assignment CASCADE;
DROP TABLE IF EXISTS waitlist_offer CASCADE;
DROP TABLE IF EXISTS waitlist_entry CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS resource CASCADE;
DROP TABLE IF EXISTS resource_features CASCADE;
DROP TABLE IF EXISTS floor CASCADE;
DROP TABLE IF EXISTS building CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- Create tables
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
    building_id INTEGER REFERENCES building(id),
    name VARCHAR(100) NOT NULL
);
CREATE TABLE resource (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    department_id INTEGER REFERENCES department(id),
    floor_id INTEGER REFERENCES floor(id),
    capacity INTEGER NOT NULL,
    desk_mode VARCHAR(20) NOT NULL
);
CREATE TABLE resource_features (
   resource_id INTEGER NOT NULL,
    feature VARCHAR(255),
    FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE
);
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INTEGER REFERENCES role(id),
    department_id INTEGER REFERENCES department(id),
    email VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE booking (
    id SERIAL PRIMARY KEY,
    resource_id INTEGER REFERENCES resource(id),
    user_id INTEGER REFERENCES user_account(id),
    start_utc TIMESTAMP NOT NULL,
    end_utc TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);
CREATE TABLE waitlist_entry (
    id SERIAL PRIMARY KEY,
    resource_id INTEGER REFERENCES resource(id),
    start_utc TIMESTAMP NOT NULL,
    end_utc TIMESTAMP NOT NULL,
    user_id INTEGER REFERENCES user_account(id),
    created_at_utc TIMESTAMP NOT NULL
);
CREATE TABLE waitlist_offer (
    id SERIAL PRIMARY KEY,
    waitlist_entry_id INTEGER REFERENCES waitlist_entry(id),
    status VARCHAR(20) NOT NULL,
    offered_at_utc TIMESTAMP NOT NULL,
    expires_at_utc TIMESTAMP NOT NULL
);
CREATE TABLE desk_assignment (
    id SERIAL PRIMARY KEY,
    resource_id INTEGER REFERENCES resource(id),
    user_id INTEGER REFERENCES user_account(id),
    start_utc TIMESTAMP NOT NULL,
    end_utc TIMESTAMP
);
-- Seed roles
INSERT INTO role (name) VALUES ('SYSTEM_ADMIN'), ('DEPARTMENT_ADMIN'), ('USER');
-- Seed departments
INSERT INTO department (name) VALUES ('Engineering'), ('HR'), ('Sales');
-- Seed buildings and floors
INSERT INTO building (name) VALUES ('Main Office');
INSERT INTO floor (building_id, name) VALUES (1, 'First Floor');
-- Seed resources
INSERT INTO resource (name, type, department_id, floor_id, capacity, desk_mode) VALUES ('Room 101', 'ROOM', 1, 1, 10,  'HOT_DESK');
INSERT INTO resource (name, type, department_id, floor_id, capacity, desk_mode) VALUES ('Desk A1', 'DESK', 1, 1, 1,'ASSIGNED');
INSERT INTO resource (name, type, department_id, floor_id, capacity, desk_mode) VALUES ('Parking P1', 'PARKING', 2, 1, 1,'HOT_DESK');
INSERT INTO resource_features (resource_id, feature)
VALUES
    (1, 'Projector'),
    (1, 'Whiteboard');
INSERT INTO resource_features (resource_id, feature)
VALUES
    (2, 'Projector'),
    (2, 'Whiteboard');
INSERT INTO resource_features (resource_id, feature)
VALUES
    (3, 'Projector'),
    (3, 'Whiteboard');
-- Seed users
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('sysadmin', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 1, 1, 'sysadmin@vertexspace.com');
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('deptadmin', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 2, 1, 'deptadmin@vertexspace.com');
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('user1', '$2a$10$ln/UwjJaxRukCpDs9bm2AuISD6wFWKwpr8AJTxxpF1iPY6W9LflE2', 3, 1, 'user1@vertexspace.com');
-- Note: Replace dummyhash with real bcrypt hashes in production.
