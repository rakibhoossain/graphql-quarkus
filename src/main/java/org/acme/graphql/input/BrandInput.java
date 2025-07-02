package org.acme.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * GraphQL input type for Brand creation and updates
 */
public class BrandInput {

    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
    public String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    public String description;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    public String logoUrl;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    public String websiteUrl;

    public Boolean active = true;

    // Default constructor
    public BrandInput() {}

    // Constructor with required fields
    public BrandInput(String name) {
        this.name = name;
    }

    // Constructor with all fields
    public BrandInput(String name, String description, String logoUrl, String websiteUrl, Boolean active) {
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.active = active;
    }
}
