package org.acme.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * GraphQL input type for Category creation and updates
 */
public class CategoryInput {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    public String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    public String description;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    public String slug;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    public String imageUrl;

    public Boolean active = true;

    public Integer sortOrder = 0;

    public Long parentId;

    // Default constructor
    public CategoryInput() {}

    // Constructor with required fields
    public CategoryInput(String name) {
        this.name = name;
    }

    // Constructor with name and parent
    public CategoryInput(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    // Constructor with all fields
    public CategoryInput(String name, String description, String slug, String imageUrl, 
                        Boolean active, Integer sortOrder, Long parentId) {
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.imageUrl = imageUrl;
        this.active = active;
        this.sortOrder = sortOrder;
        this.parentId = parentId;
    }
}
