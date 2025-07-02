-- Sample data for ecommerce system

-- Insert sample brands
INSERT INTO brands (id, name, description, logo_url, website_url, active, created_at, updated_at) VALUES 
(1, 'Apple', 'Technology company known for innovative products', 'https://example.com/apple-logo.png', 'https://apple.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Samsung', 'South Korean multinational electronics company', 'https://example.com/samsung-logo.png', 'https://samsung.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Nike', 'American multinational corporation engaged in design and manufacturing of footwear', 'https://example.com/nike-logo.png', 'https://nike.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Adidas', 'German multinational corporation that designs and manufactures shoes', 'https://example.com/adidas-logo.png', 'https://adidas.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Sony', 'Japanese multinational conglomerate corporation', 'https://example.com/sony-logo.png', 'https://sony.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample categories (hierarchical structure)
INSERT INTO categories (id, name, description, slug, image_url, active, sort_order, parent_id, created_at, updated_at) VALUES 
-- Root categories
(1, 'Electronics', 'Electronic devices and gadgets', 'electronics', 'https://example.com/electronics.jpg', true, 1, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Clothing', 'Apparel and fashion items', 'clothing', 'https://example.com/clothing.jpg', true, 2, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Sports', 'Sports equipment and accessories', 'sports', 'https://example.com/sports.jpg', true, 3, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electronics subcategories
(4, 'Smartphones', 'Mobile phones and accessories', 'smartphones', 'https://example.com/smartphones.jpg', true, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Laptops', 'Portable computers', 'laptops', 'https://example.com/laptops.jpg', true, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Tablets', 'Tablet computers', 'tablets', 'https://example.com/tablets.jpg', true, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Audio', 'Audio equipment and accessories', 'audio', 'https://example.com/audio.jpg', true, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Clothing subcategories
(8, 'Men''s Clothing', 'Clothing for men', 'mens-clothing', 'https://example.com/mens-clothing.jpg', true, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Women''s Clothing', 'Clothing for women', 'womens-clothing', 'https://example.com/womens-clothing.jpg', true, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Shoes', 'Footwear for all', 'shoes', 'https://example.com/shoes.jpg', true, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Sports subcategories
(11, 'Running', 'Running gear and equipment', 'running', 'https://example.com/running.jpg', true, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Fitness', 'Fitness equipment and accessories', 'fitness', 'https://example.com/fitness.jpg', true, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products
INSERT INTO products (id, name, description, sku, slug, price, compare_at_price, stock_quantity, low_stock_threshold, weight, weight_unit, active, featured, track_inventory, brand_id, category_id, created_at, updated_at) VALUES 
-- Apple products
(1, 'iPhone 15 Pro', 'Latest iPhone with advanced features', 'IPHONE-15-PRO-128', 'iphone-15-pro', 999.00, 1099.00, 50, 10, 0.187, 'kg', true, true, true, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MacBook Pro 14"', 'Professional laptop with M3 chip', 'MBP-14-M3-512', 'macbook-pro-14', 1999.00, 2199.00, 25, 5, 1.6, 'kg', true, true, true, 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'iPad Air', 'Powerful and versatile tablet', 'IPAD-AIR-256', 'ipad-air', 599.00, 649.00, 30, 8, 0.461, 'kg', true, false, true, 1, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Samsung products
(4, 'Galaxy S24 Ultra', 'Premium Android smartphone', 'GALAXY-S24-ULTRA-256', 'galaxy-s24-ultra', 1199.00, 1299.00, 40, 10, 0.232, 'kg', true, true, true, 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Galaxy Tab S9', 'High-performance Android tablet', 'GALAXY-TAB-S9-128', 'galaxy-tab-s9', 799.00, 899.00, 20, 5, 0.498, 'kg', true, false, true, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Nike products
(6, 'Air Max 270', 'Comfortable running shoes', 'NIKE-AM270-BLK-10', 'nike-air-max-270', 150.00, 180.00, 100, 20, 0.8, 'kg', true, true, true, 3, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Dri-FIT Running Shirt', 'Moisture-wicking running shirt', 'NIKE-DFIT-SHIRT-M', 'nike-dri-fit-shirt', 35.00, 45.00, 200, 30, 0.15, 'kg', true, false, true, 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Adidas products
(8, 'Ultraboost 22', 'Premium running shoes', 'ADIDAS-UB22-WHT-9', 'adidas-ultraboost-22', 180.00, 200.00, 75, 15, 0.85, 'kg', true, true, true, 4, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Training Shorts', 'Comfortable training shorts', 'ADIDAS-TRAIN-SHORT-L', 'adidas-training-shorts', 40.00, 50.00, 150, 25, 0.2, 'kg', true, false, true, 4, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Sony products
(10, 'WH-1000XM5', 'Noise-canceling wireless headphones', 'SONY-WH1000XM5-BLK', 'sony-wh-1000xm5', 399.00, 449.00, 60, 12, 0.25, 'kg', true, true, true, 5, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert product images
INSERT INTO product_images (product_id, image_url) VALUES 
(1, 'https://example.com/iphone-15-pro-1.jpg'),
(1, 'https://example.com/iphone-15-pro-2.jpg'),
(1, 'https://example.com/iphone-15-pro-3.jpg'),
(2, 'https://example.com/macbook-pro-14-1.jpg'),
(2, 'https://example.com/macbook-pro-14-2.jpg'),
(3, 'https://example.com/ipad-air-1.jpg'),
(4, 'https://example.com/galaxy-s24-ultra-1.jpg'),
(4, 'https://example.com/galaxy-s24-ultra-2.jpg'),
(5, 'https://example.com/galaxy-tab-s9-1.jpg'),
(6, 'https://example.com/nike-air-max-270-1.jpg'),
(6, 'https://example.com/nike-air-max-270-2.jpg'),
(7, 'https://example.com/nike-dri-fit-shirt-1.jpg'),
(8, 'https://example.com/adidas-ultraboost-22-1.jpg'),
(8, 'https://example.com/adidas-ultraboost-22-2.jpg'),
(9, 'https://example.com/adidas-training-shorts-1.jpg'),
(10, 'https://example.com/sony-wh-1000xm5-1.jpg'),
(10, 'https://example.com/sony-wh-1000xm5-2.jpg');

-- Insert product tags
INSERT INTO product_tags (product_id, tag) VALUES 
(1, 'smartphone'),
(1, 'ios'),
(1, 'premium'),
(1, 'camera'),
(2, 'laptop'),
(2, 'professional'),
(2, 'apple-silicon'),
(2, 'portable'),
(3, 'tablet'),
(3, 'creative'),
(3, 'portable'),
(4, 'smartphone'),
(4, 'android'),
(4, 'premium'),
(4, 's-pen'),
(5, 'tablet'),
(5, 'android'),
(5, 'productivity'),
(6, 'shoes'),
(6, 'running'),
(6, 'comfort'),
(6, 'air-max'),
(7, 'shirt'),
(7, 'running'),
(7, 'moisture-wicking'),
(7, 'dri-fit'),
(8, 'shoes'),
(8, 'running'),
(8, 'boost'),
(8, 'premium'),
(9, 'shorts'),
(9, 'training'),
(9, 'comfort'),
(10, 'headphones'),
(10, 'wireless'),
(10, 'noise-canceling'),
(10, 'premium');

-- Set sequence values for auto-increment
ALTER SEQUENCE brands_seq RESTART WITH 6;
ALTER SEQUENCE categories_seq RESTART WITH 13;
ALTER SEQUENCE products_seq RESTART WITH 11;
