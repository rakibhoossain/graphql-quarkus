package org.acme.graphql;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;
import org.acme.graphql.exception.ExceptionMapper;
import org.acme.graphql.input.ProductInput;
import org.acme.service.OptimizedProductService;
import org.acme.service.ProductService;
import org.eclipse.microprofile.graphql.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * GraphQL API for Product operations
 */
@GraphQLApi
public class ProductGraphQLResource {

    @Inject
    ProductService productService;

    @Inject
    OptimizedProductService optimizedProductService;

    @Inject
    ExceptionMapper exceptionMapper;

    // Queries
    
    @Query("product")
    @Description("Get a product by ID")
    public Product getProduct(@Name("id") Long id) {
        return productService.findProductById(id);
    }

    @Query("productBySlug")
    @Description("Get a product by slug")
    public Optional<Product> getProductBySlug(@Name("slug") String slug) {
        return productService.findProductBySlug(slug);
    }

    @Query("productBySku")
    @Description("Get a product by SKU")
    public Optional<Product> getProductBySku(@Name("sku") String sku) {
        return productService.findProductBySku(sku);
    }

    @Query("products")
    @Description("Get all active products")
    public List<Product> getAllProducts() {
        return productService.getAllActiveProducts();
    }

    @Query("productsWithPagination")
    @Description("Get all active products with pagination")
    public List<Product> getAllProductsWithPagination(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.getAllActiveProducts(pageIndex, pageSize);
    }

    // Optimized queries for basic fields only
    @Query("productsBasic")
    @Description("Get products with basic fields only (optimized)")
    public List<Product> getProductsBasic(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return optimizedProductService.getProductsBasic(pageIndex, pageSize);
    }

    @Query("productsWithBrandAndCategory")
    @Description("Get products with brand and category information (selective loading)")
    public List<Product> getProductsWithBrandAndCategory(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return optimizedProductService.getProductsWithBrandAndCategory(pageIndex, pageSize);
    }

    @Query("searchProductsBasic")
    @Description("Search products with basic fields only (optimized)")
    public List<Product> searchProductsBasic(
            @Name("namePattern") String namePattern,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return optimizedProductService.searchProductsBasic(namePattern, pageIndex, pageSize);
    }

    @Query("searchProductsWithBrandAndCategory")
    @Description("Search products with brand and category information")
    public List<Product> searchProductsWithBrandAndCategory(
            @Name("namePattern") String namePattern,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return optimizedProductService.searchProductsWithBrandAndCategory(namePattern, pageIndex, pageSize);
    }

    @Query("featuredProductsBasic")
    @Description("Get featured products with basic fields only")
    public List<Product> getFeaturedProductsBasic(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return optimizedProductService.getFeaturedProductsBasic(pageIndex, pageSize);
    }

    @Query("featuredProducts")
    @Description("Get featured products")
    public List<Product> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    @Query("featuredProductsWithPagination")
    @Description("Get featured products with pagination")
    public List<Product> getFeaturedProductsWithPagination(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.getFeaturedProducts(pageIndex, pageSize);
    }

    @Query("productsByCategory")
    @Description("Get products by category ID")
    public List<Product> getProductsByCategory(@Name("categoryId") Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @Query("productsByCategoryWithPagination")
    @Description("Get products by category with pagination")
    public List<Product> getProductsByCategoryWithPagination(
            @Name("categoryId") Long categoryId,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.getProductsByCategory(categoryId, pageIndex, pageSize);
    }

    @Query("productsByBrand")
    @Description("Get products by brand ID")
    public List<Product> getProductsByBrand(@Name("brandId") Long brandId) {
        return productService.getProductsByBrand(brandId);
    }

    @Query("productsByBrandWithPagination")
    @Description("Get products by brand with pagination")
    public List<Product> getProductsByBrandWithPagination(
            @Name("brandId") Long brandId,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.getProductsByBrand(brandId, pageIndex, pageSize);
    }

    @Query("searchProducts")
    @Description("Search products by name pattern")
    public List<Product> searchProducts(@Name("namePattern") String namePattern) {
        return productService.searchProductsByName(namePattern);
    }

    @Query("searchProductsWithPagination")
    @Description("Search products by name pattern with pagination")
    public List<Product> searchProductsWithPagination(
            @Name("namePattern") String namePattern,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.searchProductsByName(namePattern, pageIndex, pageSize);
    }

    @Query("productsByPriceRange")
    @Description("Get products by price range")
    public List<Product> getProductsByPriceRange(
            @Name("minPrice") BigDecimal minPrice,
            @Name("maxPrice") BigDecimal maxPrice) {
        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @Query("productsByPriceRangeWithPagination")
    @Description("Get products by price range with pagination")
    public List<Product> getProductsByPriceRangeWithPagination(
            @Name("minPrice") BigDecimal minPrice,
            @Name("maxPrice") BigDecimal maxPrice,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return productService.getProductsByPriceRange(minPrice, maxPrice, pageIndex, pageSize);
    }

    @Query("lowStockProducts")
    @Description("Get products with low stock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @Query("outOfStockProducts")
    @Description("Get out of stock products")
    public List<Product> getOutOfStockProducts() {
        return productService.getOutOfStockProducts();
    }

    @Query("inStockProducts")
    @Description("Get in stock products")
    public List<Product> getInStockProducts() {
        return productService.getInStockProducts();
    }

    @Query("productStatistics")
    @Description("Get product statistics")
    public ProductService.ProductStatistics getProductStatistics() {
        return productService.getProductStatistics();
    }

    // Mutations

    @Mutation("createProduct")
    @Description("Create a new product")
    public Product createProduct(@Name("input") @Valid ProductInput input) {
        try {
            Product product = new Product();
            product.name = input.name;
            product.description = input.description;
            product.sku = input.sku;
            product.slug = input.slug;
            product.price = input.price;
            product.compareAtPrice = input.compareAtPrice;
            product.stockQuantity = input.stockQuantity;
            product.lowStockThreshold = input.lowStockThreshold;
            product.weight = input.weight;
            product.weightUnit = input.weightUnit;
            product.active = input.active;
            product.featured = input.featured;
            product.trackInventory = input.trackInventory;
            product.imageUrls = input.imageUrls;
            product.tags = input.tags;

            // Set relationships
            if (input.categoryId != null) {
                Category category = new Category();
                category.id = input.categoryId;
                product.category = category;
            }
            if (input.brandId != null) {
                Brand brand = new Brand();
                brand.id = input.brandId;
                product.brand = brand;
            }

            return productService.createProduct(product);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("updateProduct")
    @Description("Update an existing product")
    public Product updateProduct(@Name("id") Long id, @Name("input") @Valid ProductInput input) {
        try {
            Product productData = new Product();
            productData.name = input.name;
            productData.description = input.description;
            productData.sku = input.sku;
            productData.slug = input.slug;
            productData.price = input.price;
            productData.compareAtPrice = input.compareAtPrice;
            productData.weight = input.weight;
            productData.weightUnit = input.weightUnit;
            productData.lowStockThreshold = input.lowStockThreshold;
            productData.trackInventory = input.trackInventory;
            productData.imageUrls = input.imageUrls;
            productData.tags = input.tags;

            // Set relationships
            if (input.categoryId != null) {
                Category category = new Category();
                category.id = input.categoryId;
                productData.category = category;
            }
            if (input.brandId != null) {
                Brand brand = new Brand();
                brand.id = input.brandId;
                productData.brand = brand;
            }

            return productService.updateProduct(id, productData);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("activateProduct")
    @Description("Activate a product")
    public Product activateProduct(@Name("id") Long id) {
        return productService.activateProduct(id);
    }

    @Mutation("deactivateProduct")
    @Description("Deactivate a product")
    public Product deactivateProduct(@Name("id") Long id) {
        return productService.deactivateProduct(id);
    }

    @Mutation("setProductFeatured")
    @Description("Set product featured status")
    public Product setProductFeatured(@Name("id") Long id, @Name("featured") boolean featured) {
        return productService.setFeaturedStatus(id, featured);
    }

    @Mutation("updateProductStock")
    @Description("Update product stock quantity")
    public Product updateProductStock(@Name("id") Long id, @Name("quantity") int quantity) {
        return productService.updateStock(id, quantity);
    }

    @Mutation("addProductStock")
    @Description("Add stock to product")
    public Product addProductStock(@Name("id") Long id, @Name("quantity") int quantity) {
        return productService.addStock(id, quantity);
    }

    @Mutation("reduceProductStock")
    @Description("Reduce stock from product")
    public Product reduceProductStock(@Name("id") Long id, @Name("quantity") int quantity) {
        return productService.reduceStock(id, quantity);
    }

    @Mutation("deleteProduct")
    @Description("Delete a product (soft delete)")
    public Boolean deleteProduct(@Name("id") Long id) {
        productService.deleteProduct(id);
        return true;
    }
}
