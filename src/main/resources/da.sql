SELECT * FROM users;
SELECT * FROM roles;
SELECT * FROM users_roles;

SELECT id, username, password, enabled FROM users WHERE username = 'admin';