INSERT INTO authority (name) VALUES ('ROLE_ADMIN');
INSERT INTO authority (name) VALUES ('ROLE_USER');

INSERT INTO users (id, password_hash, fullname, mobile_number, email, created_by, last_modified_by) VALUES
(1, '$2a$10$XAiFLni.1jfz4/WI99hAu.Y/bqOOyHDsOT8hAhs/ZKwTkzr63ygtm', 'john', '07080899933', 'admin@foody.com', 'system', 'system');

INSERT INTO users (id, password_hash, fullname, mobile_number, email, created_by, last_modified_by) VALUES
(2, '$2a$10$XAiFLni.1jfz4/WI99hAu.Y/bqOOyHDsOT8hAhs/ZKwTkzr63ygtm', 'jane', '07080899934', 'dev@foody.com', 'system', 'system');

INSERT INTO user_authority (user_id, authority_name) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(2, 'ROLE_USER');
