package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity operations
 */
@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    /**
     * Find product by slug
     */
    public Optional<Product> findBySlug(String slug) {
        return find("slug = ?1", slug).firstResultOptional();
    }

    /**
     * Find product by SKU
     */
    public Optional<Product> findBySku(String sku) {
        return find("sku = ?1", sku).firstResultOptional();
    }

    /**
     * Find all active products
     */
    public List<Product> findAllActive() {
        return find("active = true", Sort.by("name")).list();
    }

    /**
     * Find all active products with pagination
     */
    public List<Product> findAllActive(Page page) {
        return find("active = true", Sort.by("name")).page(page).list();
    }

    /**
     * Find featured products
     */
    public List<Product> findFeatured() {
        return find("featured = true AND active = true", Sort.by("name")).list();
    }

    /**
     * Find featured products with pagination
     */
    public List<Product> findFeatured(Page page) {
        return find("featured = true AND active = true", Sort.by("name")).page(page).list();
    }

    /**
     * Find products by category
     */
    public List<Product> findByCategory(Category category) {
        return find("category = ?1 AND active = true", Sort.by("name"), category).list();
    }

    /**
     * Find products by category with pagination
     */
    public List<Product> findByCategory(Category category, Page page) {
        return find("category = ?1 AND active = true", Sort.by("name"), category).page(page).list();
    }

    /**
     * Find products by category ID
     */
    public List<Product> findByCategoryId(Long categoryId) {
        return find("category.id = ?1 AND active = true", Sort.by("name"), categoryId).list();
    }

    /**
     * Find products by brand
     */
    public List<Product> findByBrand(Brand brand) {
        return find("brand = ?1 AND active = true", Sort.by("name"), brand).list();
    }

    /**
     * Find products by brand with pagination
     */
    public List<Product> findByBrand(Brand brand, Page page) {
        return find("brand = ?1 AND active = true", Sort.by("name"), brand).page(page).list();
    }

    /**
     * Find products by brand ID
     */
    public List<Product> findByBrandId(Long brandId) {
        return find("brand.id = ?1 AND active = true", Sort.by("name"), brandId).list();
    }

    /**
     * Find products by name pattern (case-insensitive)
     */
    public List<Product> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", 
                   "%" + namePattern + "%", Sort.by("name")).list();
    }

    /**
     * Find products by name pattern with pagination
     */
    public List<Product> findByNameContaining(String namePattern, Page page) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true",
                   "%" + namePattern + "%").page(page).list();
    }

    /**
     * Find products by price range
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price >= ?1 AND price <= ?2 AND active = true", 
                   Sort.by("price"), minPrice, maxPrice).list();
    }

    /**
     * Find products by price range with pagination
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Page page) {
        return find("price >= ?1 AND price <= ?2 AND active = true", 
                   Sort.by("price"), minPrice, maxPrice).page(page).list();
    }

    /**
     * Find products with low stock
     */
    public List<Product> findLowStock() {
        return find("trackInventory = true AND stockQuantity <= lowStockThreshold AND active = true", 
                   Sort.by("stockQuantity")).list();
    }

    /**
     * Find out of stock products
     */
    public List<Product> findOutOfStock() {
        return find("trackInventory = true AND stockQuantity = 0 AND active = true", 
                   Sort.by("name")).list();
    }

    /**
     * Find in stock products
     */
    public List<Product> findInStock() {
        return find("(trackInventory = false OR stockQuantity > 0) AND active = true", 
                   Sort.by("name")).list();
    }

    /**
     * Check if product slug exists
     */
    public boolean existsBySlug(String slug) {
        return count("slug = ?1", slug) > 0;
    }

    /**
     * Check if product slug exists excluding specific product ID
     */
    public boolean existsBySlugExcludingId(String slug, Long excludeId) {
        return count("slug = ?1 AND id != ?2", slug, excludeId) > 0;
    }

    /**
     * Check if product SKU exists
     */
    public boolean existsBySku(String sku) {
        return count("sku = ?1", sku) > 0;
    }

    /**
     * Check if product SKU exists excluding specific product ID
     */
    public boolean existsBySkuExcludingId(String sku, Long excludeId) {
        return count("sku = ?1 AND id != ?2", sku, excludeId) > 0;
    }

    /**
     * Count active products
     */
    public long countActive() {
        return count("active = true");
    }

    /**
     * Count featured products
     */
    public long countFeatured() {
        return count("featured = true AND active = true");
    }

    /**
     * Count products by category
     */
    public long countByCategory(Category category) {
        return count("category = ?1 AND active = true", category);
    }

    /**
     * Count products by brand
     */
    public long countByBrand(Brand brand) {
        return count("brand = ?1 AND active = true", brand);
    }

    /**
     * Count low stock products
     */
    public long countLowStock() {
        return count("trackInventory = true AND stockQuantity <= lowStockThreshold AND active = true");
    }

    /**
     * Count out of stock products
     */
    public long countOutOfStock() {
        return count("trackInventory = true AND stockQuantity = 0 AND active = true");
    }

    /**
     * Soft delete product (set active to false)
     */
    public void softDelete(Long productId) {
        update("active = false WHERE id = ?1", productId);
    }

    /**
     * Activate product
     */
    public void activate(Long productId) {
        update("active = true WHERE id = ?1", productId);
    }

    /**
     * Set featured status
     */
    public void setFeatured(Long productId, boolean featured) {
        update("featured = ?1 WHERE id = ?2", featured, productId);
    }

    /**
     * Update stock quantity
     */
    public void updateStock(Long productId, int quantity) {
        update("stockQuantity = ?1 WHERE id = ?2", quantity, productId);
    }

    /**
     * Bulk update stock quantities
     */
    public int bulkUpdateStock(List<Long> productIds, int quantity) {
        return update("stockQuantity = ?1 WHERE id IN ?2", quantity, productIds);
    }

    /**
     * Find recently created products
     */
    public List<Product> findRecentlyCreated(int limit) {
        return find("active = true", Sort.by("createdAt").descending()).page(0, limit).list();
    }

    /**
     * Find recently updated products
     */
    public List<Product> findRecentlyUpdated(int limit) {
        return find("active = true", Sort.by("updatedAt").descending()).page(0, limit).list();
    }

    /**
     * Find best selling products (placeholder - would need order data)
     */
    public List<Product> findBestSelling(int limit) {
        // This would typically join with order/sales data
        // For now, return featured products as a placeholder
        return find("featured = true AND active = true", Sort.by("name")).page(0, limit).list();
    }
}
