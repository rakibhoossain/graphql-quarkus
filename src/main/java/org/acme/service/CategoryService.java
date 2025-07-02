package org.acme.service;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.acme.entity.Category;
import org.acme.repository.CategoryRepository;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Service for Category business logic operations
 */
@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    /**
     * Create a new category
     */
    @Transactional
    public Category createCategory(@Valid @NotNull Category category) {
        validateCategoryForCreation(category);
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Create a new category with parent
     */
    @Transactional
    public Category createCategory(@Valid @NotNull Category category, @NotNull Long parentId) {
        Category parent = findCategoryById(parentId);
        category.parent = parent;
        validateCategoryForCreation(category);
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Update an existing category
     */
    @Transactional
    public Category updateCategory(@NotNull Long categoryId, @Valid @NotNull Category categoryData) {
        Category existingCategory = findCategoryById(categoryId);
        validateCategoryForUpdate(categoryData, categoryId);
        
        // Update fields
        existingCategory.name = categoryData.name;
        existingCategory.description = categoryData.description;
        existingCategory.slug = categoryData.slug;
        existingCategory.imageUrl = categoryData.imageUrl;
        existingCategory.sortOrder = categoryData.sortOrder;
        
        categoryRepository.persist(existingCategory);
        return existingCategory;
    }

    /**
     * Move category to different parent
     */
    @Transactional
    public Category moveCategory(@NotNull Long categoryId, Long newParentId) {
        Category category = findCategoryById(categoryId);
        
        if (newParentId != null) {
            Category newParent = findCategoryById(newParentId);
            validateCategoryMove(category, newParent);
            category.parent = newParent;
        } else {
            category.parent = null;
        }
        
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Find category by ID
     */
    public Category findCategoryById(@NotNull Long categoryId) {
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
    }

    /**
     * Find category by slug
     */
    public Optional<Category> findCategoryBySlug(@NotNull String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /**
     * Find category by name
     */
    public Optional<Category> findCategoryByName(@NotNull String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * Get all active categories
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllActive();
    }

    /**
     * Get all active categories with pagination
     */
    public List<Category> getAllActiveCategories(int pageIndex, int pageSize) {
        return categoryRepository.findAllActive(Page.of(pageIndex, pageSize));
    }

    /**
     * Get root categories (categories without parent)
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    /**
     * Get root categories with pagination
     */
    public List<Category> getRootCategories(int pageIndex, int pageSize) {
        return categoryRepository.findRootCategories(Page.of(pageIndex, pageSize));
    }

    /**
     * Get child categories by parent
     */
    public List<Category> getChildCategories(@NotNull Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    /**
     * Get category hierarchy (all descendants)
     */
    public List<Category> getCategoryHierarchy(@NotNull Long categoryId) {
        Category category = findCategoryById(categoryId);
        return categoryRepository.findDescendants(category);
    }

    /**
     * Get category path from root to category
     */
    public List<Category> getCategoryPath(@NotNull Long categoryId) {
        Category category = findCategoryById(categoryId);
        return categoryRepository.getCategoryPath(category);
    }

    /**
     * Search categories by name pattern
     */
    public List<Category> searchCategoriesByName(@NotNull String namePattern) {
        return categoryRepository.findByNameContaining(namePattern);
    }

    /**
     * Search categories by name pattern with pagination
     */
    public List<Category> searchCategoriesByName(@NotNull String namePattern, int pageIndex, int pageSize) {
        return categoryRepository.findByNameContaining(namePattern, Page.of(pageIndex, pageSize));
    }

    /**
     * Get categories with products
     */
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithProducts();
    }

    /**
     * Get categories without products
     */
    public List<Category> getCategoriesWithoutProducts() {
        return categoryRepository.findCategoriesWithoutProducts();
    }

    /**
     * Activate category
     */
    @Transactional
    public Category activateCategory(@NotNull Long categoryId) {
        Category category = findCategoryById(categoryId);
        category.activate();
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Deactivate category
     */
    @Transactional
    public Category deactivateCategory(@NotNull Long categoryId) {
        Category category = findCategoryById(categoryId);
        category.deactivate();
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Update category sort order
     */
    @Transactional
    public Category updateSortOrder(@NotNull Long categoryId, @NotNull Integer sortOrder) {
        Category category = findCategoryById(categoryId);
        category.sortOrder = sortOrder;
        categoryRepository.persist(category);
        return category;
    }

    /**
     * Soft delete category (deactivate)
     */
    @Transactional
    public void deleteCategory(@NotNull Long categoryId) {
        Category category = findCategoryById(categoryId);
        
        // Check if category has active children
        if (categoryRepository.countByParent(category) > 0) {
            throw new BusinessException("Cannot delete category with active child categories");
        }
        
        category.deactivate();
        categoryRepository.persist(category);
    }

    /**
     * Get category statistics
     */
    public CategoryStatistics getCategoryStatistics() {
        long totalActive = categoryRepository.countActive();
        long totalRoot = categoryRepository.countRootCategories();
        long totalWithProducts = categoryRepository.findCategoriesWithProducts().size();
        long totalWithoutProducts = categoryRepository.findCategoriesWithoutProducts().size();
        
        return new CategoryStatistics(totalActive, totalRoot, totalWithProducts, totalWithoutProducts);
    }

    // Validation methods
    private void validateCategoryForCreation(Category category) {
        if (categoryRepository.existsBySlug(category.slug)) {
            throw new BusinessException("Category with slug '" + category.slug + "' already exists");
        }
        if (categoryRepository.existsByName(category.name)) {
            throw new BusinessException("Category with name '" + category.name + "' already exists");
        }
    }

    private void validateCategoryForUpdate(Category category, Long categoryId) {
        if (categoryRepository.existsBySlugExcludingId(category.slug, categoryId)) {
            throw new BusinessException("Category with slug '" + category.slug + "' already exists");
        }
        if (categoryRepository.existsByNameExcludingId(category.name, categoryId)) {
            throw new BusinessException("Category with name '" + category.name + "' already exists");
        }
    }

    private void validateCategoryMove(Category category, Category newParent) {
        // Prevent circular references
        if (isDescendantOf(newParent, category)) {
            throw new BusinessException("Cannot move category to its own descendant");
        }
    }

    private boolean isDescendantOf(Category potentialDescendant, Category ancestor) {
        if (potentialDescendant == null) return false;
        if (potentialDescendant.equals(ancestor)) return true;
        return isDescendantOf(potentialDescendant.parent, ancestor);
    }

    /**
     * Category statistics data class
     */
    public static class CategoryStatistics {
        public final long totalActive;
        public final long totalRoot;
        public final long totalWithProducts;
        public final long totalWithoutProducts;

        public CategoryStatistics(long totalActive, long totalRoot, long totalWithProducts, long totalWithoutProducts) {
            this.totalActive = totalActive;
            this.totalRoot = totalRoot;
            this.totalWithProducts = totalWithProducts;
            this.totalWithoutProducts = totalWithoutProducts;
        }
    }
}
