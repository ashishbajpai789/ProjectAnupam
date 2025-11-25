-- ============================================
-- PROJECT ANUPAM - DATABASE SETUP SCRIPT
-- ============================================

-- Create Database
CREATE DATABASE IF NOT EXISTS project_anupam;
USE project_anupam;

-- ============================================
-- DROP EXISTING TABLES (if you want fresh start)
-- ============================================
-- Uncomment these if you want to reset database

-- DROP TABLE IF EXISTS tokens;
-- DROP TABLE IF EXISTS order_items;
-- DROP TABLE IF EXISTS orders;
-- DROP TABLE IF EXISTS products;
-- DROP TABLE IF EXISTS students;

-- ============================================
-- CREATE TABLES
-- ============================================

-- Admin Table
CREATE TABLE admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_admin_email (email)
);

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Products Table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    on_sale BOOLEAN NOT NULL DEFAULT FALSE,
    sale_price DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_category (category),
    INDEX idx_active (active),
    INDEX idx_on_sale (on_sale),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_email (customer_email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tokens Table
CREATE TABLE IF NOT EXISTS tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    token TEXT NOT NULL,
    is_expired BOOLEAN NOT NULL DEFAULT FALSE,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_user_id_type (user_id, user_type),
    INDEX idx_token_validity (is_expired, is_revoked),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indices for better query performance
CREATE INDEX idx_admins_active ON admin(active);
CREATE INDEX idx_students_active ON students(active);
CREATE INDEX  idx_products_active ON products(active);

-- Update any NULL values to TRUE (if any exist)
UPDATE admin SET active = TRUE WHERE active IS NULL;
UPDATE students SET active = TRUE WHERE active IS NULL;
UPDATE products SET active = TRUE WHERE active IS NULL;

-- ============================================
-- INSERT SAMPLE DATA
-- ============================================

-- Sample Admin
-- Password: "admin123"

INSERT INTO admin (name, email, password)
VALUES ('Abhay Raj', 'abhayraj@gmail.com', '$2a$10$jxUfqG2/Mkm4.uVhjw2bm.eqIPPpVQGZ77CabFKdFHjD5hsKHYvle');

-- Sample Students
-- Password: "student123" (BCrypt encoded)

INSERT INTO students (name, email, password, phone, active) VALUES
('Priya Sharma', 'priya@student.com', '$2a$10$ESPKcSKKH1y9DmGpPICyT.oNpcahHfSOv8awQ1WDW1fOQZE8zLmRe', '9876543210', TRUE),
('Rahul Kumar', 'rahul@student.com', '$2a$10$ESPKcSKKH1y9DmGpPICyT.oNpcahHfSOv8awQ1WDW1fOQZE8zLmRe', '9876543211', TRUE),
('Ananya Verma', 'ananya@student.com', '$2a$10$ESPKcSKKH1y9DmGpPICyT.oNpcahHfSOv8awQ1WDW1fOQZE8zLmRe', '9876543212', TRUE),
('Arjun Singh', 'arjun@student.com', '$2a$10$ESPKcSKKH1y9DmGpPICyT.oNpcahHfSOv8awQ1WDW1fOQZE8zLmRe', '9876543213', TRUE);

-- Sample Products
INSERT INTO products (student_id, name, description, price, quantity, category, image, active, on_sale, sale_price) VALUES
-- Priya's Products (student_id = 1)
(1, 'Embroidered Bookmark', 'Beautiful handmade embroidered bookmark with traditional Indian motifs', 60.00, 50, 'Bookmarks', '/images/products/bookmark1.jpg', TRUE, FALSE, NULL),
(1, 'Pearl Bracelet', 'Elegant pearl bracelet handcrafted with care and attention to detail', 150.00, 30, 'Jewelry', '/images/products/pearl-bracelet.jpg', TRUE, TRUE, 120.00),
(1, 'Decorative Wall Hanging', 'Handcrafted decorative wall hanging for home decor', 200.00, 20, 'Home Decor', '/images/products/wall-hanging.jpg', TRUE, FALSE, NULL),

-- Rahul's Products (student_id = 2)
(2, 'Loom Bracelet', 'Colorful handmade loom bracelet with unique patterns', 50.00, 100, 'Jewelry', '/images/products/loom-bracelet.jpg', TRUE, FALSE, NULL),
(2, 'Handmade Towel', 'Soft and absorbent handmade towel with embroidered border', 80.00, 40, 'Home Decor', '/images/products/towel.jpg', TRUE, FALSE, NULL),
(2, 'Painted Keychain', 'Hand-painted wooden keychain with vibrant colors', 40.00, 80, 'Accessories', '/images/products/keychain.jpg', TRUE, TRUE, 30.00),

-- Ananya's Products (student_id = 3)
(3, 'Clay Pot Set', 'Handcrafted terracotta clay pot set for plants', 120.00, 25, 'Home Decor', '/images/products/clay-pot.jpg', TRUE, FALSE, NULL),
(3, 'Macrame Plant Hanger', 'Beautiful macrame plant hanger for indoor plants', 90.00, 35, 'Home Decor', '/images/products/macrame.jpg', TRUE, TRUE, 75.00),
(3, 'Decorative Candle Holder', 'Hand-painted decorative candle holder', 110.00, 30, 'Home Decor', '/images/products/candle-holder.jpg', TRUE, FALSE, NULL),

-- Arjun's Products (student_id = 4)
(4, 'Leather Wallet', 'Handcrafted genuine leather wallet with multiple card slots', 250.00, 15, 'Accessories', '/images/products/wallet.jpg', TRUE, FALSE, NULL),
(4, 'Notebook Cover', 'Handmade leather-bound notebook cover', 180.00, 20, 'Stationery', '/images/products/notebook-cover.jpg', TRUE, TRUE, 150.00),
(4, 'Pen Holder', 'Elegant wooden pen holder with carved designs', 95.00, 40, 'Stationery', '/images/products/pen-holder.jpg', TRUE, FALSE, NULL);

-- Sample Orders (Optional - for testing)
INSERT INTO orders (customer_name, customer_email, customer_phone, address, status, total_amount) VALUES
('Amit Patel', 'amit@example.com', '9988776655', '123, MG Road, Delhi, 110001', 'DELIVERED', 270.00),
('Sneha Reddy', 'sneha@example.com', '9988776656', '456, Brigade Road, Bangalore, 560001', 'SHIPPED', 180.00),
('Vikram Malhotra', 'vikram@example.com', '9988776657', '789, Park Street, Kolkata, 700016', 'PROCESSING', 120.00);

-- Sample Order Items (Optional - for testing)
INSERT INTO order_items (order_id, product_id, student_id, quantity, price) VALUES
-- Order 1: Amit's order
(1, 1, 1, 2, 60.00),   -- 2 Bookmarks
(1, 2, 1, 1, 120.00),  -- 1 Pearl Bracelet (on sale)
(1, 4, 2, 1, 50.00),   -- 1 Loom Bracelet

-- Order 2: Sneha's order
(2, 5, 2, 2, 80.00),   -- 2 Handmade Towels
(2, 6, 2, 1, 30.00),   -- 1 Painted Keychain (on sale)

-- Order 3: Vikram's order
(3, 2, 1, 1, 120.00);  -- 1 Pearl Bracelet (on sale)

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check all students
SELECT * FROM students;

-- Check all products
SELECT
    p.id,
    p.name,
    p.price,
    p.sale_price,
    p.quantity,
    p.category,
    p.on_sale,
    p.active,
    s.name as student_name
FROM products p
JOIN students s ON p.student_id = s.id
ORDER BY p.created_at DESC;

-- Check products on sale
SELECT
    p.name,
    p.price,
    p.sale_price,
    p.quantity,
    s.name as student_name
FROM products p
JOIN students s ON p.student_id = s.id
WHERE p.on_sale = TRUE AND p.active = TRUE;

-- Check all orders with items
SELECT
    o.id as order_id,
    o.customer_name,
    o.status,
    o.total_amount,
    oi.quantity,
    p.name as product_name,
    s.name as student_name
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
JOIN students s ON oi.student_id = s.id
ORDER BY o.created_at DESC;

-- Check student sales
SELECT
    s.name as student_name,
    COUNT(DISTINCT oi.order_id) as total_orders,
    SUM(oi.quantity) as total_items_sold,
    SUM(oi.quantity * oi.price) as total_revenue
FROM students s
LEFT JOIN order_items oi ON s.id = oi.student_id
GROUP BY s.id, s.name
ORDER BY total_revenue DESC;

-- ============================================
-- USEFUL MAINTENANCE QUERIES
-- ============================================

-- Deactivate out-of-stock products
UPDATE products
SET active = FALSE
WHERE quantity = 0;

-- Find products low on stock
SELECT
    p.name,
    p.quantity,
    s.name as student_name,
    s.email as student_email
FROM products p
JOIN students s ON p.student_id = s.id
WHERE p.quantity > 0 AND p.quantity < 10
ORDER BY p.quantity ASC;

-- Get monthly sales report
SELECT
    DATE_FORMAT(o.created_at, '%Y-%m') as month,
    COUNT(*) as total_orders,
    SUM(o.total_amount) as total_revenue
FROM orders o
GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
ORDER BY month DESC;

-- Clean up expired tokens (run periodically)
DELETE FROM tokens
WHERE expires_at < NOW()
   OR is_expired = TRUE
   OR is_revoked = TRUE;

-- ============================================
-- ADMIN CREDENTIALS REMINDER
-- ============================================

/*
ADMIN LOGIN (Not in Database - Configured in application.properties):
----------------------------------------------------------------------
Email: admin@projectanupam.com
Password: admin123

Add to application.properties:
admin.email=admin@projectanupam.com
admin.password=$2a$10$[YOUR_BCRYPT_HASH_HERE]

Generate BCrypt hash at: https://bcrypt-generator.com/


STUDENT LOGIN (In Database):
-----------------------------
All students have password: student123

1. Priya Sharma
   Email: priya@student.com
   Password: student123

2. Rahul Kumar
   Email: rahul@student.com
   Password: student123

3. Ananya Verma
   Email: ananya@student.com
   Password: student123

4. Arjun Singh
   Email: arjun@student.com
   Password: student123

NOTE: The password hashes in the INSERT statements above are placeholders.
Generate real BCrypt hashes using the PasswordHashGenerator or online tool!
*/

-- ============================================
-- DATABASE BACKUP COMMAND
-- ============================================

/*
To backup database:
mysqldump -u root -p project_anupam > project_anupam_backup.sql

To restore database:
mysql -u root -p project_anupam < project_anupam_backup.sql
*/