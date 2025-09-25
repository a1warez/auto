-- Создание таблицы ролей (roles)
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,  -- SERIAL - автоинкрементный integer
                                     name VARCHAR(50) NOT NULL UNIQUE
);

-- Создание таблицы пользователей (users)
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,  -- Храните пароли зашифрованными (BCrypt, Argon2)
                                     email VARCHAR(100) NOT NULL,
                                     enabled BOOLEAN NOT NULL DEFAULT TRUE  -- Активен ли пользователь
);

-- Создание таблицы связей пользователей и ролей (users_roles)
CREATE TABLE IF NOT EXISTS users_roles (
                                           user_id INTEGER NOT NULL,
                                           role_id INTEGER NOT NULL,
                                           PRIMARY KEY (user_id, role_id), -- Составной первичный ключ
                                           FOREIGN KEY (user_id) REFERENCES users(id),
                                           FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Создание таблицы товаров (products)
CREATE TABLE IF NOT EXISTS products (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
                                        description TEXT,
                                        price DECIMAL(10, 2) NOT NULL, -- DECIMAL для точного хранения денежных значений
                                        stock_quantity INTEGER NOT NULL DEFAULT 0,
                                        image_url VARCHAR(255),
                                        brand VARCHAR(100),
                                        part_number VARCHAR(100),
                                        compatible_cars TEXT
);

-- Вставка ролей (если их еще нет)
INSERT INTO roles (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

INSERT INTO roles (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

-- Вставка пользователя-администратора (если его еще нет)
-- !!! ВАЖНО: Замените '$2a$10$...' на захешированный пароль
INSERT INTO users (username, password, email, enabled)
SELECT 'admin', '$2a$10$s0a8hbj2/VB9skc/VVq98.Qowaiy1nYqHKvJmNkN.rzzq0OUzjOkG', 'admin@example.com', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Связывание администратора с ролью ADMIN (если ее еще нет)
INSERT INTO users_roles (user_id, role_id)
SELECT (SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
WHERE NOT EXISTS (SELECT 1 FROM users_roles WHERE user_id = (SELECT id FROM users WHERE username = 'admin') AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));

-- Пример добавления товара (для демонстрации)
INSERT INTO products (name, description, price, stock_quantity, brand, part_number)
VALUES ('Тормозные колодки', 'Высококачественные тормозные колодки для легковых автомобилей', 49.99, 100, 'Bosch', '0986AB1234');

-- Создание таблицы для информации об автомобилях
CREATE TABLE IF NOT EXISTS car_models (
                                          id SERIAL PRIMARY KEY,
                                          make VARCHAR(50) NOT NULL,   -- Марка автомобиля (например, Toyota)
                                          model VARCHAR(50) NOT NULL,  -- Модель автомобиля (например, Camry)
                                          year INTEGER NOT NULL,       -- Год выпуска (например, 2020)
                                          engine_type VARCHAR(50)      -- Тип двигателя (например, 2.5L I4)
);

-- Добавление тестовой информации об автомобиле
INSERT INTO car_models (make, model, year, engine_type)
VALUES ('Toyota', 'Camry', 2020, '2.5L I4');

-- Создание таблицы для связи товаров и автомобилей
CREATE TABLE IF NOT EXISTS part_compatibility (
                                                  product_id INTEGER NOT NULL,
                                                  car_model_id INTEGER NOT NULL,
                                                  PRIMARY KEY (product_id, car_model_id),
                                                  FOREIGN KEY (product_id) REFERENCES products(id),
                                                  FOREIGN KEY (car_model_id) REFERENCES car_models(id)
);

-- Добавление связи между тормозными колодками и Toyota Camry 2020
INSERT INTO part_compatibility (product_id, car_model_id)
SELECT (SELECT id FROM products WHERE part_number = '0986AB1234'), (SELECT id FROM car_models WHERE make = 'Toyota' AND model = 'Camry' AND year = 2020);
-- Вставляем тестового пользователя-админа (необязательно для регистрации, но полезно для тестирования)
-- !!! ВАЖНО: Пароль должен быть захеширован. Для теста можно использовать BCryptPasswordEncoder.
-- Например, чтобы захешировать "admin123":
-- В временном Java коде:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String encodedPassword = encoder.encode("admin123");
-- System.out.println(encodedPassword); // скопируйте результат сюда
