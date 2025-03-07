INSERT INTO authority (name) VALUES ('ROLE_ADMIN');
INSERT INTO authority (name) VALUES ('ROLE_USER');

INSERT INTO users (id, password_hash, fullname, email, activated, lang_key, created_by, last_modified_by) VALUES
(1, '$2a$12$SHyHLPRGbB10rFgEDyRUGe00vTU1TIRMVhrGKdMgAoM37uFvl/pZe', 'Admin John', 'admin@foody.com', true, 'en', 'system', 'system');

INSERT INTO users (id, password_hash, fullname, email, activated, lang_key, created_by, last_modified_by) VALUES
(2, '$2a$12$SHyHLPRGbB10rFgEDyRUGe00vTU1TIRMVhrGKdMgAoM37uFvl/pZe', 'Admin Jane', 'dev@foody.com', true, 'en','system', 'system');

INSERT INTO user_authority (user_id, authority_name) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER');
