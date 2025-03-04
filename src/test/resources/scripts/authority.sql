INSERT INTO authority (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

