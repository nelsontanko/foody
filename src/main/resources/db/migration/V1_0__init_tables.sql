CREATE SEQUENCE IF NOT EXISTS user_sequence START WITH 50 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS address_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE authority (
  name VARCHAR(50) NOT NULL,
   CONSTRAINT pk_authority PRIMARY KEY (name)
);

CREATE TABLE user_authority (
  authority_name VARCHAR(50) NOT NULL,
   user_id BIGINT NOT NULL,
   CONSTRAINT pk_user_authority PRIMARY KEY (authority_name, user_id)
);

CREATE TABLE address (
  id BIGINT NOT NULL,
   user_id BIGINT,
   street VARCHAR(255) NOT NULL,
   city VARCHAR(255) NOT NULL,
   state VARCHAR(255) NOT NULL,
   postal_code VARCHAR(255),
   country VARCHAR(255) NOT NULL,
   type VARCHAR(255),
   is_default BOOLEAN NOT NULL,
   CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE users (
  id BIGINT NOT NULL,
   fullname VARCHAR(50),
   email VARCHAR(254) NOT NULL,
   mobile_number VARCHAR(15),
   reset_key VARCHAR(20),
   reset_date TIMESTAMP WITHOUT TIME ZONE,
   status VARCHAR(255),
   created_by VARCHAR(50) NOT NULL,
   created_date TIMESTAMP WITHOUT TIME ZONE,
   last_modified_by VARCHAR(100),
   last_modified_date TIMESTAMP WITHOUT TIME ZONE,
   password_hash VARCHAR(60) NOT NULL,
   CONSTRAINT pk_users PRIMARY KEY (id)
);