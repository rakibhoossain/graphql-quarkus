package org.acme.graphql.util;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing GraphQL field selections
 */
@ApplicationScoped
public class GraphQLFieldSelector {

    /**
     * Extract all requested field names from GraphQL query
     */
    public Set<String> getRequestedFields(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().getFields().stream()
            .map(SelectedField::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Extract requested fields at a specific depth
     */
    public Set<String> getRequestedFields(DataFetchingEnvironment environment, String depth) {
        return environment.getSelectionSet().getFields(depth).stream()
            .map(SelectedField::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Check if a specific field is requested
     */
    public boolean isFieldRequested(DataFetchingEnvironment environment, String fieldName) {
        return environment.getSelectionSet().contains(fieldName);
    }

    /**
     * Check if any relationship fields are requested
     */
    public boolean hasRelationshipFields(DataFetchingEnvironment environment) {
        Set<String> fields = getRequestedFields(environment);
        return fields.contains("brand") || 
               fields.contains("category") || 
               fields.contains("products") ||
               fields.contains("children") ||
               fields.contains("parent");
    }

    /**
     * Get requested fields for nested objects
     */
    public Set<String> getNestedFields(DataFetchingEnvironment environment, String parentField) {
        return environment.getSelectionSet().getFields().stream()
            .filter(field -> field.getName().equals(parentField))
            .flatMap(field -> field.getSelectionSet().getFields().stream())
            .map(SelectedField::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Determine fetch strategy based on requested fields
     */
    public FetchStrategy determineFetchStrategy(DataFetchingEnvironment environment) {
        Set<String> fields = getRequestedFields(environment);
        
        // If only basic fields are requested, use projection
        if (isOnlyBasicFields(fields)) {
            return FetchStrategy.PROJECTION;
        }
        
        // If relationships are requested, use entity with joins
        if (hasRelationshipFields(environment)) {
            return FetchStrategy.ENTITY_WITH_JOINS;
        }
        
        // Default to entity fetch
        return FetchStrategy.ENTITY;
    }

    /**
     * Check if only basic (non-relationship) fields are requested
     */
    private boolean isOnlyBasicFields(Set<String> fields) {
        return fields.stream().noneMatch(field -> 
            field.equals("brand") || 
            field.equals("category") || 
            field.equals("products") ||
            field.equals("children") ||
            field.equals("parent") ||
            field.equals("imageUrls") ||
            field.equals("tags")
        );
    }

    /**
     * Get optimized field list for JPA queries
     */
    public Set<String> getOptimizedFieldList(DataFetchingEnvironment environment) {
        Set<String> requestedFields = getRequestedFields(environment);
        
        // Always include ID for entity identity
        requestedFields.add("id");
        
        // Filter out GraphQL meta fields
        return requestedFields.stream()
            .filter(field -> !field.startsWith("__"))
            .collect(Collectors.toSet());
    }

    /**
     * Check if pagination info is requested
     */
    public boolean isPaginationRequested(DataFetchingEnvironment environment) {
        return environment.getSelectionSet().contains("pageInfo") ||
               environment.getSelectionSet().contains("totalCount");
    }

    /**
     * Enum for different fetch strategies
     */
    public enum FetchStrategy {
        PROJECTION,        // Use projections for basic fields only
        ENTITY,           // Fetch full entity without joins
        ENTITY_WITH_JOINS // Fetch entity with necessary joins
    }
}
