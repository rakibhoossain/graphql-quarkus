-- Test data for ecommerce system

-- Insert test brands
INSERT INTO brands (id, name, description, active, created_at, updated_at) VALUES 
(1, 'Test Brand 1', 'Test brand description 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Brand 2', 'Test brand description 2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test categories
INSERT INTO categories (id, name, description, slug, active, sort_order, parent_id, created_at, updated_at) VALUES 
(1, 'Test Category 1', 'Test category description 1', 'test-category-1', true, 1, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Category 2', 'Test category description 2', 'test-category-2', true, 2, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Test Subcategory 1', 'Test subcategory description 1', 'test-subcategory-1', true, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test products
INSERT INTO products (id, name, description, sku, slug, price, stock_quantity, active, featured, track_inventory, brand_id, category_id, created_at, updated_at) VALUES 
(1, 'Test Product 1', 'Test product description 1', 'TEST-PROD-1', 'test-product-1', 99.99, 10, true, true, true, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Product 2', 'Test product description 2', 'TEST-PROD-2', 'test-product-2', 149.99, 5, true, false, true, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Set sequence values for auto-increment
ALTER SEQUENCE brands_seq RESTART WITH 3;
ALTER SEQUENCE categories_seq RESTART WITH 4;
ALTER SEQUENCE products_seq RESTART WITH 3;
