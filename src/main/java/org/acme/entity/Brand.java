package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Brand entity representing product brands in the ecommerce system
 */
@Entity
@Table(name = "brands")
public class Brand extends PanacheEntity {

    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    public String description;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    @Column(name = "logo_url")
    public String logoUrl;

    @Size(max = 255, message = "Website URL cannot exceed 255 characters")
    @Column(name = "website_url")
    public String websiteUrl;

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    // Relationship with products
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Product> products;

    // Default constructor
    public Brand() {}

    // Constructor with required fields
    public Brand(String name) {
        this.name = name;
    }

    // Constructor with name and description
    public Brand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Custom finder methods
    public static Brand findByName(String name) {
        return find("name", name).firstResult();
    }

    public static List<Brand> findActive() {
        return find("active", true).list();
    }

    public static List<Brand> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + namePattern + "%").list();
    }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
