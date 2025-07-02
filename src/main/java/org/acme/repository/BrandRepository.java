package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Brand;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Brand entity operations
 */
@ApplicationScoped
public class BrandRepository implements PanacheRepository<Brand> {

    /**
     * Find brand by name (case-insensitive)
     */
    public Optional<Brand> findByName(String name) {
        return find("LOWER(name) = LOWER(?1)", name).firstResultOptional();
    }

    /**
     * Find all active brands
     */
    public List<Brand> findAllActive() {
        return find("active = true", Sort.by("name")).list();
    }

    /**
     * Find all active brands with pagination
     */
    public List<Brand> findAllActive(Page page) {
        return find("active = true", Sort.by("name")).page(page).list();
    }

    /**
     * Find brands by name pattern (case-insensitive)
     */
    public List<Brand> findByNameContaining(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", 
                   "%" + namePattern + "%", Sort.by("name")).list();
    }

    /**
     * Find brands by name pattern with pagination
     */
    public List<Brand> findByNameContaining(String namePattern, Page page) {
        return find("LOWER(name) LIKE LOWER(?1) AND active = true", 
                   Sort.by("name"), "%" + namePattern + "%").page(page).list();
    }

    /**
     * Check if brand name exists (case-insensitive)
     */
    public boolean existsByName(String name) {
        return count("LOWER(name) = LOWER(?1)", name) > 0;
    }

    /**
     * Check if brand name exists excluding specific brand ID
     */
    public boolean existsByNameExcludingId(String name, Long excludeId) {
        return count("LOWER(name) = LOWER(?1) AND id != ?2", name, excludeId) > 0;
    }

    /**
     * Count active brands
     */
    public long countActive() {
        return count("active = true");
    }

    /**
     * Count brands by name pattern
     */
    public long countByNameContaining(String namePattern) {
        return count("LOWER(name) LIKE LOWER(?1) AND active = true", "%" + namePattern + "%");
    }

    /**
     * Find brands with products
     */
    public List<Brand> findBrandsWithProducts() {
        return find("SELECT DISTINCT b FROM Brand b JOIN b.products p WHERE b.active = true AND p.active = true", 
                   Sort.by("name")).list();
    }

    /**
     * Find brands without products
     */
    public List<Brand> findBrandsWithoutProducts() {
        return find("SELECT b FROM Brand b WHERE b.active = true AND " +
                   "(b.products IS EMPTY OR NOT EXISTS (SELECT p FROM Product p WHERE p.brand = b AND p.active = true))", 
                   Sort.by("name")).list();
    }

    /**
     * Soft delete brand (set active to false)
     */
    public void softDelete(Long brandId) {
        update("active = false WHERE id = ?1", brandId);
    }

    /**
     * Activate brand
     */
    public void activate(Long brandId) {
        update("active = true WHERE id = ?1", brandId);
    }

    /**
     * Bulk activate brands
     */
    public int activateBrands(List<Long> brandIds) {
        return update("active = true WHERE id IN ?1", brandIds);
    }

    /**
     * Bulk deactivate brands
     */
    public int deactivateBrands(List<Long> brandIds) {
        return update("active = false WHERE id IN ?1", brandIds);
    }

    /**
     * Find recently created brands
     */
    public List<Brand> findRecentlyCreated(int limit) {
        return find("active = true", Sort.by("createdAt").descending()).page(0, limit).list();
    }

    /**
     * Find recently updated brands
     */
    public List<Brand> findRecentlyUpdated(int limit) {
        return find("active = true", Sort.by("updatedAt").descending()).page(0, limit).list();
    }
}
