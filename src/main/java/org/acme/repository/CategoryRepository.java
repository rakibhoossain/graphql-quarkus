package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity operations
 */
@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {

    /**
     * Find category by slug
     */
    public Optional<Category> findBySlug(String slug) {
        return find("slug = ?1", slug).firstResultOptional();
    }

    /**
     * Find category by name (case-insensitive)
     */
    public Optional<Category> findByName(String name) {
        return find("LOWER(name) = LOWER(?1)", name).firstResultOptional();
    }

    /**
     * Find all active categories
     */
    public List<Category> findAllActive() {
        return find("active = true", Sort.by("sortOrder", "name")).list();
    }

    /**
     * Find all active categories with pagination
     */
    public List<Category> findAllActive(Page page) {
        return find("active = true", Sort.by("sortOrder", "name")).page(page).list();
    }

    /**
     * Find root categories (categories without parent)
     */
    public List<Category> findRootCategories() {
        return find("parent IS NULL AND active = true", Sort.by("sortOrder", "name")).list();
    }

    /**
     * Find root categories with pagination
     */
    public List<Category> findRootCategories(Page page) {
        return find("parent IS NULL AND active = true", Sort.by("sortOrder", "name")).page(page).list();
    }

    /**
     * Find child categories by parent
     */
    public List<Category> findByParent(Category parent) {
        return find("parent = ?1 AND active = true", Sort.by("sortOrder", "name"), parent).list();
    }

    /**
     * Find child categories by parent ID
     */
    public List<Category> findByParentId(Long parentId) {
        return find("parent.id = ?1 AND active = true", Sort.by("sortOrder", "name"), parentId).list();
    }

    /**
     * Find categories by name pattern (case-insensitive)
     */
    public List<Category> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", 
                   "%" + namePattern + "%", Sort.by("name")).list();
    }

    /**
     * Find categories by name pattern with pagination
     */
    public List<Category> findByNameContaining(String namePattern, Page page) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", 
                   Sort.by("name"), "%" + namePattern + "%").page(page).list();
    }

    /**
     * Check if category slug exists
     */
    public boolean existsBySlug(String slug) {
        return count("slug = ?1", slug) > 0;
    }

    /**
     * Check if category slug exists excluding specific category ID
     */
    public boolean existsBySlugExcludingId(String slug, Long excludeId) {
        return count("slug = ?1 AND id != ?2", slug, excludeId) > 0;
    }

    /**
     * Check if category name exists (case-insensitive)
     */
    public boolean existsByName(String name) {
        return count("LOWER(name) = LOWER(?1)", name) > 0;
    }

    /**
     * Check if category name exists excluding specific category ID
     */
    public boolean existsByNameExcludingId(String name, Long excludeId) {
        return count("LOWER(name) = LOWER(?1) AND id != ?2", name, excludeId) > 0;
    }

    /**
     * Count active categories
     */
    public long countActive() {
        return count("active = true");
    }

    /**
     * Count root categories
     */
    public long countRootCategories() {
        return count("parent IS NULL AND active = true");
    }

    /**
     * Count child categories by parent
     */
    public long countByParent(Category parent) {
        return count("parent = ?1 AND active = true", parent);
    }

    /**
     * Find categories with products
     */
    public List<Category> findCategoriesWithProducts() {
        return find("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE c.active = true AND p.active = true", 
                   Sort.by("name")).list();
    }

    /**
     * Find categories without products
     */
    public List<Category> findCategoriesWithoutProducts() {
        return find("SELECT c FROM Category c WHERE c.active = true AND " +
                   "(c.products IS EMPTY OR NOT EXISTS (SELECT p FROM Product p WHERE p.category = c AND p.active = true))", 
                   Sort.by("name")).list();
    }

    /**
     * Find all descendants of a category (recursive)
     */
    public List<Category> findDescendants(Category parent) {
        return find("SELECT c FROM Category c WHERE c.parent = ?1 OR c.parent IN " +
                   "(SELECT c2 FROM Category c2 WHERE c2.parent = ?1) AND c.active = true", 
                   Sort.by("sortOrder", "name"), parent).list();
    }

    /**
     * Get category hierarchy path (from root to category)
     */
    public List<Category> getCategoryPath(Category category) {
        if (category.parent == null) {
            return List.of(category);
        }
        
        List<Category> path = getCategoryPath(category.parent);
        path.add(category);
        return path;
    }

    /**
     * Soft delete category (set active to false)
     */
    public void softDelete(Long categoryId) {
        update("active = false WHERE id = ?1", categoryId);
    }

    /**
     * Activate category
     */
    public void activate(Long categoryId) {
        update("active = true WHERE id = ?1", categoryId);
    }

    /**
     * Update sort order
     */
    public void updateSortOrder(Long categoryId, Integer sortOrder) {
        update("sortOrder = ?1 WHERE id = ?2", sortOrder, categoryId);
    }

    /**
     * Find recently created categories
     */
    public List<Category> findRecentlyCreated(int limit) {
        return find("active = true", Sort.by("createdAt").descending()).page(0, limit).list();
    }

    /**
     * Find recently updated categories
     */
    public List<Category> findRecentlyUpdated(int limit) {
        return find("active = true", Sort.by("updatedAt").descending()).page(0, limit).list();
    }
}
