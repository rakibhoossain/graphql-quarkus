package org.acme.graphql.input;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * GraphQL input type for Product creation and updates
 */
public class ProductInput {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    public String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    public String description;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    public String sku;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    public String slug;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    public BigDecimal price;

    @DecimalMin(value = "0.0", message = "Compare at price must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Compare at price must have at most 10 integer digits and 2 decimal places")
    public BigDecimal compareAtPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    public Integer stockQuantity = 0;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    public Integer lowStockThreshold = 5;

    @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
    @Digits(integer = 8, fraction = 3, message = "Weight must have at most 8 integer digits and 3 decimal places")
    public BigDecimal weight;

    @Size(max = 50, message = "Weight unit cannot exceed 50 characters")
    public String weightUnit = "kg";

    public Boolean active = true;

    public Boolean featured = false;

    public Boolean trackInventory = true;

    public List<String> imageUrls;

    public List<String> tags;

    public Long categoryId;

    public Long brandId;

    // Default constructor
    public ProductInput() {}

    // Constructor with required fields
    public ProductInput(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    // Constructor with name, price, and category
    public ProductInput(String name, BigDecimal price, Long categoryId) {
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
    }

    // Constructor with name, price, category, and brand
    public ProductInput(String name, BigDecimal price, Long categoryId, Long brandId) {
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }
}
