package org.acme.service;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;
import org.acme.repository.ProductRepository;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for Product business logic operations
 */
@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    CategoryService categoryService;

    @Inject
    BrandService brandService;

    /**
     * Create a new product
     */
    @Transactional
    public Product createProduct(@Valid @NotNull Product product) {
        validateProductForCreation(product);
        setProductRelations(product);
        productRepository.persist(product);
        return product;
    }

    /**
     * Update an existing product
     */
    @Transactional
    public Product updateProduct(@NotNull Long productId, @Valid @NotNull Product productData) {
        Product existingProduct = findProductById(productId);
        validateProductForUpdate(productData, productId);
        
        // Update fields
        existingProduct.name = productData.name;
        existingProduct.description = productData.description;
        existingProduct.sku = productData.sku;
        existingProduct.slug = productData.slug;
        existingProduct.price = productData.price;
        existingProduct.compareAtPrice = productData.compareAtPrice;
        existingProduct.weight = productData.weight;
        existingProduct.weightUnit = productData.weightUnit;
        existingProduct.lowStockThreshold = productData.lowStockThreshold;
        existingProduct.trackInventory = productData.trackInventory;
        existingProduct.imageUrls = productData.imageUrls;
        existingProduct.tags = productData.tags;
        
        // Update relations
        if (productData.category != null) {
            existingProduct.category = categoryService.findCategoryById(productData.category.id);
        }
        if (productData.brand != null) {
            existingProduct.brand = brandService.findBrandById(productData.brand.id);
        }
        
        productRepository.persist(existingProduct);
        return existingProduct;
    }

    /**
     * Find product by ID
     */
    public Product findProductById(@NotNull Long productId) {
        return productRepository.findByIdOptional(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
    }

    /**
     * Find product by slug
     */
    public Optional<Product> findProductBySlug(@NotNull String slug) {
        return productRepository.findBySlug(slug);
    }

    /**
     * Find product by SKU
     */
    public Optional<Product> findProductBySku(@NotNull String sku) {
        return productRepository.findBySku(sku);
    }

    /**
     * Get all active products
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActive();
    }

    /**
     * Get all active products with pagination
     */
    public List<Product> getAllActiveProducts(int pageIndex, int pageSize) {
        return productRepository.findAllActive(Page.of(pageIndex, pageSize));
    }

    /**
     * Get featured products
     */
    public List<Product> getFeaturedProducts() {
        return productRepository.findFeatured();
    }

    /**
     * Get featured products with pagination
     */
    public List<Product> getFeaturedProducts(int pageIndex, int pageSize) {
        return productRepository.findFeatured(Page.of(pageIndex, pageSize));
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(@NotNull Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Get products by category with pagination
     */
    public List<Product> getProductsByCategory(@NotNull Long categoryId, int pageIndex, int pageSize) {
        Category category = categoryService.findCategoryById(categoryId);
        return productRepository.findByCategory(category, Page.of(pageIndex, pageSize));
    }

    /**
     * Get products by brand
     */
    public List<Product> getProductsByBrand(@NotNull Long brandId) {
        return productRepository.findByBrandId(brandId);
    }

    /**
     * Get products by brand with pagination
     */
    public List<Product> getProductsByBrand(@NotNull Long brandId, int pageIndex, int pageSize) {
        Brand brand = brandService.findBrandById(brandId);
        return productRepository.findByBrand(brand, Page.of(pageIndex, pageSize));
    }

    /**
     * Search products by name pattern
     */
    public List<Product> searchProductsByName(@NotNull String namePattern) {
        return productRepository.findByNameContaining(namePattern);
    }

    /**
     * Search products by name pattern with pagination
     */
    public List<Product> searchProductsByName(@NotNull String namePattern, int pageIndex, int pageSize) {
        return productRepository.findByNameContaining(namePattern, Page.of(pageIndex, pageSize));
    }

    /**
     * Get products by price range
     */
    public List<Product> getProductsByPriceRange(@NotNull BigDecimal minPrice, @NotNull BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Get products by price range with pagination
     */
    public List<Product> getProductsByPriceRange(@NotNull BigDecimal minPrice, @NotNull BigDecimal maxPrice, 
                                                int pageIndex, int pageSize) {
        return productRepository.findByPriceRange(minPrice, maxPrice, Page.of(pageIndex, pageSize));
    }

    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStock();
    }

    /**
     * Get out of stock products
     */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStock();
    }

    /**
     * Get in stock products
     */
    public List<Product> getInStockProducts() {
        return productRepository.findInStock();
    }

    /**
     * Activate product
     */
    @Transactional
    public Product activateProduct(@NotNull Long productId) {
        Product product = findProductById(productId);
        product.activate();
        productRepository.persist(product);
        return product;
    }

    /**
     * Deactivate product
     */
    @Transactional
    public Product deactivateProduct(@NotNull Long productId) {
        Product product = findProductById(productId);
        product.deactivate();
        productRepository.persist(product);
        return product;
    }

    /**
     * Set featured status
     */
    @Transactional
    public Product setFeaturedStatus(@NotNull Long productId, boolean featured) {
        Product product = findProductById(productId);
        product.setFeatured(featured);
        productRepository.persist(product);
        return product;
    }

    /**
     * Update stock quantity
     */
    @Transactional
    public Product updateStock(@NotNull Long productId, int quantity) {
        Product product = findProductById(productId);
        if (!product.trackInventory) {
            throw new BusinessException("Cannot update stock for product that doesn't track inventory");
        }
        product.updateStock(quantity);
        productRepository.persist(product);
        return product;
    }

    /**
     * Add stock
     */
    @Transactional
    public Product addStock(@NotNull Long productId, int quantity) {
        Product product = findProductById(productId);
        if (!product.trackInventory) {
            throw new BusinessException("Cannot add stock for product that doesn't track inventory");
        }
        product.addStock(quantity);
        productRepository.persist(product);
        return product;
    }

    /**
     * Reduce stock
     */
    @Transactional
    public Product reduceStock(@NotNull Long productId, int quantity) {
        Product product = findProductById(productId);
        if (!product.trackInventory) {
            throw new BusinessException("Cannot reduce stock for product that doesn't track inventory");
        }
        if (product.stockQuantity < quantity) {
            throw new BusinessException("Insufficient stock. Available: " + product.stockQuantity + ", Requested: " + quantity);
        }
        product.reduceStock(quantity);
        productRepository.persist(product);
        return product;
    }

    /**
     * Soft delete product (deactivate)
     */
    @Transactional
    public void deleteProduct(@NotNull Long productId) {
        Product product = findProductById(productId);
        product.deactivate();
        productRepository.persist(product);
    }

    /**
     * Get product statistics
     */
    public ProductStatistics getProductStatistics() {
        long totalActive = productRepository.countActive();
        long totalFeatured = productRepository.countFeatured();
        long totalLowStock = productRepository.countLowStock();
        long totalOutOfStock = productRepository.countOutOfStock();
        
        return new ProductStatistics(totalActive, totalFeatured, totalLowStock, totalOutOfStock);
    }

    // Helper methods
    private void setProductRelations(Product product) {
        if (product.category != null && product.category.id != null) {
            product.category = categoryService.findCategoryById(product.category.id);
        }
        if (product.brand != null && product.brand.id != null) {
            product.brand = brandService.findBrandById(product.brand.id);
        }
    }

    // Validation methods
    private void validateProductForCreation(Product product) {
        if (product.slug != null && productRepository.existsBySlug(product.slug)) {
            throw new BusinessException("Product with slug '" + product.slug + "' already exists");
        }
        if (product.sku != null && productRepository.existsBySku(product.sku)) {
            throw new BusinessException("Product with SKU '" + product.sku + "' already exists");
        }
    }

    private void validateProductForUpdate(Product product, Long productId) {
        if (product.slug != null && productRepository.existsBySlugExcludingId(product.slug, productId)) {
            throw new BusinessException("Product with slug '" + product.slug + "' already exists");
        }
        if (product.sku != null && productRepository.existsBySkuExcludingId(product.sku, productId)) {
            throw new BusinessException("Product with SKU '" + product.sku + "' already exists");
        }
    }

    /**
     * Product statistics data class
     */
    public static class ProductStatistics {
        public final long totalActive;
        public final long totalFeatured;
        public final long totalLowStock;
        public final long totalOutOfStock;

        public ProductStatistics(long totalActive, long totalFeatured, long totalLowStock, long totalOutOfStock) {
            this.totalActive = totalActive;
            this.totalFeatured = totalFeatured;
            this.totalLowStock = totalLowStock;
            this.totalOutOfStock = totalOutOfStock;
        }
    }
}
