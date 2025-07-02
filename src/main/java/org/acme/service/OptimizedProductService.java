package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.acme.entity.Product;

import java.util.List;

/**
 * Optimized service for Product queries using Entity Graphs for field selection
 */
@ApplicationScoped
public class OptimizedProductService {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Get products with basic fields only (optimized for performance)
     */
    public List<Product> getProductsBasic(int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.basic");

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.active = true ORDER BY p.name",
            Product.class
        );

        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Get products with brand and category information (selective loading)
     */
    public List<Product> getProductsWithBrandAndCategory(int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.withBrandAndCategory");
        
        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.active = true ORDER BY p.name", 
            Product.class
        );
        
        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);
        
        return query.getResultList();
    }

    /**
     * Search products with basic fields only
     */
    public List<Product> searchProductsBasic(String namePattern, int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.basic");
        
        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:pattern) AND p.active = true ORDER BY p.name", 
            Product.class
        );
        
        query.setParameter("pattern", "%" + namePattern + "%");
        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);
        
        return query.getResultList();
    }

    /**
     * Search products with brand and category information
     */
    public List<Product> searchProductsWithBrandAndCategory(String namePattern, int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.withBrandAndCategory");

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:pattern) AND p.active = true ORDER BY p.name",
            Product.class
        );

        query.setParameter("pattern", "%" + namePattern + "%");
        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Get featured products with basic fields only
     */
    public List<Product> getFeaturedProductsBasic(int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.basic");

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.featured = true AND p.active = true ORDER BY p.name",
            Product.class
        );

        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Get products by category with basic fields only
     */
    public List<Product> getProductsByCategoryBasic(Long categoryId, int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.basic");

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true ORDER BY p.name",
            Product.class
        );

        query.setParameter("categoryId", categoryId);
        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Get products by brand with basic fields only
     */
    public List<Product> getProductsByBrandBasic(Long brandId, int pageIndex, int pageSize) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("Product.basic");

        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.active = true ORDER BY p.name",
            Product.class
        );

        query.setParameter("brandId", brandId);
        query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        query.setFirstResult(pageIndex * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Count total products for pagination
     */
    public long countActiveProducts() {
        return entityManager.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE p.active = true", 
            Long.class
        ).getSingleResult();
    }

    /**
     * Count products matching search pattern
     */
    public long countProductsByNamePattern(String namePattern) {
        return entityManager.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE LOWER(p.name) LIKE LOWER(:pattern) AND p.active = true", 
            Long.class
        )
        .setParameter("pattern", "%" + namePattern + "%")
        .getSingleResult();
    }

    /**
     * Count products by category
     */
    public long countProductsByCategory(Long categoryId) {
        return entityManager.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true", 
            Long.class
        )
        .setParameter("categoryId", categoryId)
        .getSingleResult();
    }

    /**
     * Count products by brand
     */
    public long countProductsByBrand(Long brandId) {
        return entityManager.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId AND p.active = true", 
            Long.class
        )
        .setParameter("brandId", brandId)
        .getSingleResult();
    }
}
