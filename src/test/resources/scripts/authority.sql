INSERT INTO authority (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER'),
    ('ROLE_SELLER')
ON CONFLICT (name) DO NOTHING;

