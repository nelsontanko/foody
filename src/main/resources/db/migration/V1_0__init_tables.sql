CREATE SEQUENCE IF NOT EXISTS user_sequence START WITH 50 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS address_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS order_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS order_items_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS restaurant_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS food_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS courier_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS rating_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE IF NOT EXISTS comment_sequence START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE authority (
  name VARCHAR(50) NOT NULL,
   CONSTRAINT pk_authority PRIMARY KEY (name)
);

CREATE TABLE user_authority (
  authority_name VARCHAR(50) NOT NULL,
   user_id BIGINT NOT NULL,
   CONSTRAINT pk_user_authority PRIMARY KEY (authority_name, user_id)
);

CREATE TABLE addresses (
   id BIGINT NOT NULL,
    user_id BIGINT,
    restaurant_id BIGINT,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE users (
  id BIGINT NOT NULL,
   fullname VARCHAR(50),
   email VARCHAR(254) NOT NULL,
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

CREATE TABLE orders (
   id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    order_time TIMESTAMP NOT NULL,
    estimated_delivery_time TIMESTAMP,
    delivery_address_id BIGINT NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE order_items (
   id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE restaurants (
   id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(15),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_available BOOLEAN NOT NULL DEFAULT true,
    available_from TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_restaurant PRIMARY KEY (id)
);

CREATE TABLE food (
   id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19, 2) NOT NULL,
    image_url VARCHAR(255),
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    average_rating DOUBLE PRECISION,
    total_ratings INTEGER,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_food PRIMARY KEY (id)
);

CREATE TABLE couriers (
   id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    available_from TIMESTAMP,
    restaurant_id BIGINT NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_courier PRIMARY KEY (id)
);

CREATE TABLE ratings (
   id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_rating PRIMARY KEY (id)
);

CREATE TABLE comments (
   id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

ALTER TABLE orders ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE orders ADD CONSTRAINT fk_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE orders ADD CONSTRAINT fk_delivery_address FOREIGN KEY (delivery_address_id) REFERENCES addresses(id);
ALTER TABLE order_items ADD CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE order_items ADD CONSTRAINT fk_food FOREIGN KEY (food_id) REFERENCES food(id);
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
ALTER TABLE user_authority ADD CONSTRAINT fk_on_authority FOREIGN KEY (authority_name) REFERENCES authority (name);
ALTER TABLE user_authority ADD CONSTRAINT fk_on_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE addresses ADD CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE addresses ADD CONSTRAINT fk_address_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE couriers ADD CONSTRAINT fk_courier_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE ratings ADD CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE ratings ADD CONSTRAINT fk_rating_food FOREIGN KEY (food_id) REFERENCES food(id);
ALTER TABLE ratings ADD CONSTRAINT unique_user_food_rating UNIQUE (user_id, food_id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_food FOREIGN KEY (food_id) REFERENCES food(id);

CREATE INDEX idx_address_location ON addresses (latitude, longitude);
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_restaurant ON orders(restaurant_id);
CREATE INDEX idx_order_status ON orders (status);
CREATE INDEX idx_restaurant_name ON restaurants(name);
CREATE INDEX idx_food_name ON food(name);
CREATE INDEX idx_courier_name ON couriers(name);
CREATE INDEX idx_ratings_food_id ON ratings(food_id);
CREATE INDEX idx_comments_food_id ON comments(food_id);

ALTER TABLE ratings ADD CONSTRAINT chk_rating_range CHECK (rating >= 1 AND rating <= 5);
-- Enforce positive quantities and prices
ALTER TABLE order_items ADD CONSTRAINT chk_positive_quantity CHECK (quantity > 0);
ALTER TABLE order_items ADD CONSTRAINT chk_positive_price CHECK (price > 0);
ALTER TABLE food ADD CONSTRAINT chk_positive_food_price CHECK (price > 0);


