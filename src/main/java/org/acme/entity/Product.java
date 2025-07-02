package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product entity representing products in the ecommerce system
 */
@Entity
@Table(name = "products")
@NamedEntityGraph(
    name = "Product.basic",
    attributeNodes = {
        @NamedAttributeNode("id"),
        @NamedAttributeNode("name"),
        @NamedAttributeNode("sku"),
        @NamedAttributeNode("price"),
        @NamedAttributeNode("stockQuantity")
    }
)
@NamedEntityGraph(
    name = "Product.withBrandAndCategory",
    attributeNodes = {
        @NamedAttributeNode("id"),
        @NamedAttributeNode("name"),
        @NamedAttributeNode("sku"),
        @NamedAttributeNode("price"),
        @NamedAttributeNode("stockQuantity"),
        @NamedAttributeNode(value = "brand", subgraph = "brand.basic"),
        @NamedAttributeNode(value = "category", subgraph = "category.basic")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "brand.basic",
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("name"),
                @NamedAttributeNode("logoUrl")
            }
        ),
        @NamedSubgraph(
            name = "category.basic",
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("name"),
                @NamedAttributeNode("slug")
            }
        )
    }
)
public class Product extends PanacheEntity {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Column(name = "name", nullable = false)
    public String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(name = "description", length = 2000)
    public String description;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    @Column(name = "sku", unique = true)
    public String sku;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    @Column(name = "slug", unique = true)
    public String slug;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    public BigDecimal price;

    @DecimalMin(value = "0.0", message = "Compare at price must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Compare at price must have at most 10 integer digits and 2 decimal places")
    @Column(name = "compare_at_price", precision = 12, scale = 2)
    public BigDecimal compareAtPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    public Integer stockQuantity = 0;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    @Column(name = "low_stock_threshold")
    public Integer lowStockThreshold = 5;

    @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
    @Digits(integer = 8, fraction = 3, message = "Weight must have at most 8 integer digits and 3 decimal places")
    @Column(name = "weight", precision = 11, scale = 3)
    public BigDecimal weight;

    @Size(max = 50, message = "Weight unit cannot exceed 50 characters")
    @Column(name = "weight_unit")
    public String weightUnit = "kg";

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @Column(name = "featured", nullable = false)
    public Boolean featured = false;

    @Column(name = "track_inventory", nullable = false)
    public Boolean trackInventory = true;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    public List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    public List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    public Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    public Category category;

    // Default constructor
    public Product() {}

    // Constructor with required fields
    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
        this.slug = generateSlug(name);
    }

    // Constructor with name, price, and category
    public Product(String name, BigDecimal price, Category category) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.slug = generateSlug(name);
    }

    // Constructor with name, price, category, and brand
    public Product(String name, BigDecimal price, Category category, Brand brand) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.brand = brand;
        this.slug = generateSlug(name);
    }

    // Custom finder methods
    public static Product findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }

    public static Product findBySku(String sku) {
        return find("sku", sku).firstResult();
    }

    public static List<Product> findActive() {
        return find("active", true).list();
    }

    public static List<Product> findFeatured() {
        return find("featured = true AND active = true").list();
    }

    public static List<Product> findByCategory(Category category) {
        return find("category = ?1 AND active = true", category).list();
    }

    public static List<Product> findByBrand(Brand brand) {
        return find("brand = ?1 AND active = true", brand).list();
    }

    public static List<Product> findLowStock() {
        return find("trackInventory = true AND stockQuantity <= lowStockThreshold AND active = true").list();
    }

    public static List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price >= ?1 AND price <= ?2 AND active = true", minPrice, maxPrice).list();
    }

    public static List<Product> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", "%" + namePattern + "%").list();
    }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isInStock() {
        return !trackInventory || stockQuantity > 0;
    }

    public boolean isLowStock() {
        return trackInventory && stockQuantity <= lowStockThreshold;
    }

    public void updateStock(int quantity) {
        if (trackInventory) {
            this.stockQuantity = Math.max(0, quantity);
        }
    }

    public void addStock(int quantity) {
        if (trackInventory && quantity > 0) {
            this.stockQuantity += quantity;
        }
    }

    public void reduceStock(int quantity) {
        if (trackInventory && quantity > 0) {
            this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
        }
    }

    // Helper method to generate slug from name
    private String generateSlug(String name) {
        if (name == null) return null;
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @PrePersist
    @PreUpdate
    public void updateSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug(this.name);
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
