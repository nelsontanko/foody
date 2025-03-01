SELECT setval('user_sequence', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('user_sequence', (SELECT COALESCE(MAX(id), 2) FROM users));

INSERT INTO users (id, password_hash, fullname, last_name, email, created_by, last_modified_by) VALUES
(1, 'password-hash', 'john doe', 'admin@example.com', 'system', 'system');

INSERT INTO users (id, password_hash, fullname, email, created_by, last_modified_by) VALUES
(2, 'password-hash', 'jane smith', 'user@example.com', 'system', 'system');
