INSERT INTO users (
    email,
    password_hash,
    role,
    status
) VALUES (
    'admin@example.com',
    '$2a$10$replace_with_bcrypt_hash',
    'ADMIN',
    'ACTIVE'
);
