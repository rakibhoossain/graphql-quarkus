package org.acme.graphql.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.acme.entity.Brand;
import org.acme.service.BrandService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Optimized data fetcher for Brand entities that only selects requested fields
 */
@ApplicationScoped
public class OptimizedBrandDataFetcher {

    @Inject
    EntityManager entityManager;

    @Inject
    BrandService brandService;

    /**
     * Fetch brands with only the requested fields
     */
    public List<Brand> findAllBrands(DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFields(environment);
        
        if (requestedFields.contains("products")) {
            // If products are requested, use service method with proper join fetching
            return brandService.getAllActiveBrands();
        }
        
        // Build dynamic JPQL query with only requested fields
        String jpql = buildSelectQuery(requestedFields, "Brand", "b", "b.active = true");
        
        Query query = entityManager.createQuery(jpql, Brand.class);
        return query.getResultList();
    }

    /**
     * Fetch brand by ID with only requested fields
     */
    public Brand findBrandById(Long id, DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFields(environment);
        
        if (requestedFields.contains("products")) {
            // If products are requested, use service method with proper join fetching
            return brandService.findBrandById(id);
        }
        
        // Build dynamic JPQL query with only requested fields
        String jpql = buildSelectQuery(requestedFields, "Brand", "b", "b.id = :id");
        
        Query query = entityManager.createQuery(jpql, Brand.class);
        query.setParameter("id", id);
        
        List<Brand> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Search brands with only requested fields
     */
    public List<Brand> searchBrands(String namePattern, DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFields(environment);
        
        if (requestedFields.contains("products")) {
            // If products are requested, use service method
            return brandService.searchBrandsByName(namePattern);
        }
        
        String jpql = buildSelectQuery(requestedFields, "Brand", "b", 
            "LOWER(b.name) LIKE LOWER(:pattern) AND b.active = true");
        
        Query query = entityManager.createQuery(jpql, Brand.class);
        query.setParameter("pattern", "%" + namePattern + "%");
        
        return query.getResultList();
    }

    /**
     * Extract requested fields from GraphQL query
     */
    private Set<String> getRequestedFields(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().getFields().stream()
            .map(SelectedField::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Build dynamic JPQL SELECT query based on requested fields
     */
    private String buildSelectQuery(Set<String> requestedFields, String entityName, String alias, String whereClause) {
        StringBuilder jpql = new StringBuilder("SELECT ");
        
        // Always include ID for entity identity
        if (!requestedFields.contains("id")) {
            requestedFields.add("id");
        }
        
        // Map GraphQL fields to entity fields
        List<String> selectFields = requestedFields.stream()
            .filter(field -> isValidEntityField(field))
            .map(field -> alias + "." + field)
            .collect(Collectors.toList());
        
        if (selectFields.isEmpty()) {
            // Fallback to select all if no valid fields
            jpql.append(alias);
        } else {
            jpql.append(String.join(", ", selectFields));
        }
        
        jpql.append(" FROM ").append(entityName).append(" ").append(alias);
        
        if (whereClause != null && !whereClause.isEmpty()) {
            jpql.append(" WHERE ").append(whereClause);
        }
        
        jpql.append(" ORDER BY ").append(alias).append(".name");
        
        return jpql.toString();
    }

    /**
     * Check if field is a valid entity field (not a relationship)
     */
    private boolean isValidEntityField(String field) {
        return !field.equals("products") && // Skip relationships
               !field.equals("__typename"); // Skip GraphQL meta fields
    }
}
