-- Pharmacy POS database schema (MySQL 8 / MariaDB 10.4+)
--   mysql -u <user> -p -e "CREATE DATABASE pharmacy_pos CHARACTER SET utf8mb4"
--   mysql -u <user> -p pharmacy_pos < schema.sql
-- Then add a login with:  gradlew createUser --args="admin <password> ADMIN"

CREATE TABLE IF NOT EXISTS users (
  id       INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50)  NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,              -- BCrypt hash
  role     ENUM('ADMIN','CASHIER') NOT NULL DEFAULT 'CASHIER'
);

CREATE TABLE IF NOT EXISTS medicines (
  id                    INT AUTO_INCREMENT PRIMARY KEY,
  name                  VARCHAR(150) NOT NULL,
  price                 DECIMAL(10,2) NOT NULL,
  stock_qty             INT NOT NULL DEFAULT 0,
  expiry_date           DATE,
  requires_prescription BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sales (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  sale_datetime DATETIME NOT NULL,
  total_amount  DECIMAL(10,2) NOT NULL,
  user_id       INT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sale_items (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  sale_id     INT NOT NULL,
  medicine_id INT NOT NULL,
  quantity    INT NOT NULL,
  unit_price  DECIMAL(10,2) NOT NULL,
  subtotal    DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (sale_id)     REFERENCES sales(id)     ON DELETE CASCADE,
  FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);
