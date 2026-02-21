-- VertexSpace initial schema and seed data
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
    type VARCHAR(20) NOT NULL,
    department_id INTEGER REFERENCES department(id),
    floor_id INTEGER REFERENCES floor(id),
    metadata JSONB,
    mode VARCHAR(20) NOT NULL
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
INSERT INTO role (name) VALUES ('System Admin'), ('Department Admin'), ('User');
-- Seed departments
INSERT INTO department (name) VALUES ('Engineering'), ('HR'), ('Sales');
-- Seed buildings and floors
INSERT INTO building (name) VALUES ('Main Office');
INSERT INTO floor (building_id, name) VALUES (1, 'First Floor');
-- Seed resources
INSERT INTO resource (type, department_id, floor_id, metadata, mode) VALUES ('ROOM', 1, 1, '{"capacity":10}', 'HOT DESK');
INSERT INTO resource (type, department_id, floor_id, metadata, mode) VALUES ('DESK', 1, 1, '{"capacity":1}', 'ASSIGNED');
INSERT INTO resource (type, department_id, floor_id, metadata, mode) VALUES ('PARKING', 2, 1, '{"capacity":1}', 'HOT DESK');
-- Seed users
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('sysadmin', '$2a$10$dummyhash', 1, 1, 'sysadmin@vertexspace.com');
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('deptadmin', '$2a$10$dummyhash', 2, 1, 'deptadmin@vertexspace.com');
INSERT INTO user_account (username, password, role_id, department_id, email) VALUES ('user1', '$2a$10$dummyhash', 3, 1, 'user1@vertexspace.com');
-- Note: Replace dummyhash with real bcrypt hashes in production.
