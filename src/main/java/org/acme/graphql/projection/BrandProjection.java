package org.acme.graphql.projection;

import java.time.LocalDateTime;

/**
 * Projection interface for Brand entity to fetch only specific fields
 */
public interface BrandProjection {
    Long getId();
    String getName();
    String getDescription();
    String getLogoUrl();
    String getWebsiteUrl();
    Boolean getActive();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}

/**
 * Minimal brand projection for list views
 */
interface BrandSummaryProjection {
    Long getId();
    String getName();
    String getLogoUrl();
    Boolean getActive();
}

/**
 * Brand projection with product count
 */
interface BrandWithStatsProjection extends BrandProjection {
    Long getProductCount();
}
