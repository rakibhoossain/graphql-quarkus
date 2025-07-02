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
 * Category entity representing product categories in the ecommerce system
 * Supports hierarchical categories with parent-child relationships
 */
@Entity
@Table(name = "categories")
public class Category extends PanacheEntity {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false)
    public String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    public String description;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    @Column(name = "slug", unique = true)
    public String slug;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @Column(name = "sort_order")
    public Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    // Self-referencing relationship for hierarchical categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Category> children;

    // Relationship with products
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Product> products;

    // Default constructor
    public Category() {}

    // Constructor with required fields
    public Category(String name) {
        this.name = name;
        this.slug = generateSlug(name);
    }

    // Constructor with name and description
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.slug = generateSlug(name);
    }

    // Constructor with parent category
    public Category(String name, String description, Category parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.slug = generateSlug(name);
    }

    // Custom finder methods
    public static Category findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }

    public static List<Category> findActive() {
        return find("active", true).list();
    }

    public static List<Category> findRootCategories() {
        return find("parent IS NULL AND active = true ORDER BY sortOrder, name").list();
    }

    public static List<Category> findByParent(Category parent) {
        return find("parent = ?1 AND active = true ORDER BY sortOrder, name", parent).list();
    }

    public static List<Category> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + namePattern + "%").list();
    }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isRootCategory() {
        return this.parent == null;
    }

    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    // Helper method to generate slug from name
    private String generateSlug(String name) {
        if (name == null) return null;
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @PrePersist
    @PreUpdate
    public void updateSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug(this.name);
        }
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", active=" + active +
                ", parentId=" + (parent != null ? parent.id : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}
