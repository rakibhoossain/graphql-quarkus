package org.acme.repository;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.acme.entity.Product;
import org.acme.graphql.projection.ProductProjection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Optimized repository for Product queries with field selection
 */
@ApplicationScoped
public class OptimizedProductRepository {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Find products with only requested fields
     */
    public List<Product> findProductsWithFields(Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, "p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Find products by category with field selection
     */
    public List<Product> findProductsByCategoryWithFields(Long categoryId, Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, "p.category.id = :categoryId AND p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("categoryId", categoryId);
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Find products by brand with field selection
     */
    public List<Product> findProductsByBrandWithFields(Long brandId, Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, "p.brand.id = :brandId AND p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("brandId", brandId);
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Search products with field selection
     */
    public List<Product> searchProductsWithFields(String namePattern, Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, 
            "LOWER(p.name) LIKE LOWER(:pattern) AND p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("pattern", "%" + namePattern + "%");
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Find products by price range with field selection
     */
    public List<Product> findProductsByPriceRangeWithFields(BigDecimal minPrice, BigDecimal maxPrice, 
                                                           Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, 
            "p.price >= :minPrice AND p.price <= :maxPrice AND p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Find featured products with field selection
     */
    public List<Product> findFeaturedProductsWithFields(Set<String> requestedFields, Page page) {
        String jpql = buildProductQuery(requestedFields, "p.featured = true AND p.active = true", true);
        
        Query query = entityManager.createQuery(jpql, Product.class);
        
        if (page != null) {
            query.setFirstResult(page.index * page.size);
            query.setMaxResults(page.size);
        }
        
        return query.getResultList();
    }

    /**
     * Build optimized JPQL query based on requested fields
     */
    private String buildProductQuery(Set<String> requestedFields, String whereClause, boolean includeJoins) {
        StringBuilder jpql = new StringBuilder("SELECT ");
        
        // Determine what to fetch based on requested fields
        boolean needsBrand = requestedFields.contains("brand");
        boolean needsCategory = requestedFields.contains("category");
        boolean needsImages = requestedFields.contains("imageUrls");
        boolean needsTags = requestedFields.contains("tags");
        
        // Always select the main entity
        jpql.append("p");
        
        jpql.append(" FROM Product p");
        
        // Add joins only if needed
        if (needsBrand && includeJoins) {
            jpql.append(" LEFT JOIN FETCH p.brand");
        }
        if (needsCategory && includeJoins) {
            jpql.append(" LEFT JOIN FETCH p.category");
        }
        if (needsImages && includeJoins) {
            jpql.append(" LEFT JOIN FETCH p.imageUrls");
        }
        if (needsTags && includeJoins) {
            jpql.append(" LEFT JOIN FETCH p.tags");
        }
        
        if (whereClause != null && !whereClause.isEmpty()) {
            jpql.append(" WHERE ").append(whereClause);
        }
        
        jpql.append(" ORDER BY p.name");
        
        return jpql.toString();
    }

    /**
     * Get product projections for lightweight queries
     */
    public List<ProductProjection> findProductProjections(Set<String> requestedFields) {
        // Build a projection query that only selects needed fields
        StringBuilder jpql = new StringBuilder("SELECT ");
        
        // Map requested fields to projection constructor
        if (requestedFields.contains("brand") || requestedFields.contains("category")) {
            jpql.append("p.id, p.name, p.description, p.sku, p.slug, p.price, p.compareAtPrice, ")
                .append("p.stockQuantity, p.active, p.featured, p.createdAt, p.updatedAt, ")
                .append("b.id, b.name, b.logoUrl, ")
                .append("c.id, c.name, c.slug");
            
            jpql.append(" FROM Product p")
                .append(" LEFT JOIN p.brand b")
                .append(" LEFT JOIN p.category c")
                .append(" WHERE p.active = true")
                .append(" ORDER BY p.name");
        } else {
            // Simple projection without relationships
            jpql.append("p.id, p.name, p.description, p.sku, p.slug, p.price, p.compareAtPrice, ")
                .append("p.stockQuantity, p.active, p.featured, p.createdAt, p.updatedAt");
            
            jpql.append(" FROM Product p")
                .append(" WHERE p.active = true")
                .append(" ORDER BY p.name");
        }
        
        Query query = entityManager.createQuery(jpql.toString());
        return query.getResultList();
    }
}
