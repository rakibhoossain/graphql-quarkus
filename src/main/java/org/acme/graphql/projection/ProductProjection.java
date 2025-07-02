package org.acme.graphql.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection interface for Product entity to fetch only specific fields
 */
public interface ProductProjection {
    Long getId();
    String getName();
    String getDescription();
    String getSku();
    String getSlug();
    BigDecimal getPrice();
    BigDecimal getCompareAtPrice();
    Integer getStockQuantity();
    Boolean getActive();
    Boolean getFeatured();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    
    // Nested projections for relationships
    ProductBrandProjection getBrand();
    ProductCategoryProjection getCategory();
}

/**
 * Minimal product projection for list views
 */
interface ProductSummaryProjection {
    Long getId();
    String getName();
    String getSku();
    BigDecimal getPrice();
    Integer getStockQuantity();
    Boolean getActive();
    Boolean getFeatured();
}

/**
 * Product projection for search results
 */
interface ProductSearchProjection {
    Long getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    String getSlug();
    Boolean getActive();
    ProductBrandProjection getBrand();
    ProductCategoryProjection getCategory();
}

/**
 * Minimal brand projection for nested queries in products
 */
interface ProductBrandProjection {
    Long getId();
    String getName();
    String getLogoUrl();
}

/**
 * Minimal category projection for nested queries in products
 */
interface ProductCategoryProjection {
    Long getId();
    String getName();
    String getSlug();
}
