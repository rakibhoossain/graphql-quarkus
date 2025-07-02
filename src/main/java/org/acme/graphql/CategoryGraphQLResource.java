package org.acme.graphql;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.acme.entity.Category;
import org.acme.graphql.exception.ExceptionMapper;
import org.acme.graphql.input.CategoryInput;
import org.acme.service.CategoryService;
import org.eclipse.microprofile.graphql.*;

import java.util.List;
import java.util.Optional;

/**
 * GraphQL API for Category operations
 */
@GraphQLApi
public class CategoryGraphQLResource {

    @Inject
    CategoryService categoryService;

    @Inject
    ExceptionMapper exceptionMapper;

    // Queries
    
    @Query("category")
    @Description("Get a category by ID")
    public Category getCategory(@Name("id") Long id) {
        return categoryService.findCategoryById(id);
    }

    @Query("categoryBySlug")
    @Description("Get a category by slug")
    public Optional<Category> getCategoryBySlug(@Name("slug") String slug) {
        return categoryService.findCategoryBySlug(slug);
    }

    @Query("categoryByName")
    @Description("Get a category by name")
    public Optional<Category> getCategoryByName(@Name("name") String name) {
        return categoryService.findCategoryByName(name);
    }

    @Query("categories")
    @Description("Get all active categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllActiveCategories();
    }

    @Query("categoriesWithPagination")
    @Description("Get all active categories with pagination")
    public List<Category> getAllCategoriesWithPagination(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return categoryService.getAllActiveCategories(pageIndex, pageSize);
    }

    @Query("rootCategories")
    @Description("Get root categories (categories without parent)")
    public List<Category> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @Query("rootCategoriesWithPagination")
    @Description("Get root categories with pagination")
    public List<Category> getRootCategoriesWithPagination(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return categoryService.getRootCategories(pageIndex, pageSize);
    }

    @Query("childCategories")
    @Description("Get child categories by parent ID")
    public List<Category> getChildCategories(@Name("parentId") Long parentId) {
        return categoryService.getChildCategories(parentId);
    }

    @Query("categoryHierarchy")
    @Description("Get category hierarchy (all descendants)")
    public List<Category> getCategoryHierarchy(@Name("categoryId") Long categoryId) {
        return categoryService.getCategoryHierarchy(categoryId);
    }

    @Query("categoryPath")
    @Description("Get category path from root to category")
    public List<Category> getCategoryPath(@Name("categoryId") Long categoryId) {
        return categoryService.getCategoryPath(categoryId);
    }

    @Query("searchCategories")
    @Description("Search categories by name pattern")
    public List<Category> searchCategories(@Name("namePattern") String namePattern) {
        return categoryService.searchCategoriesByName(namePattern);
    }

    @Query("searchCategoriesWithPagination")
    @Description("Search categories by name pattern with pagination")
    public List<Category> searchCategoriesWithPagination(
            @Name("namePattern") String namePattern,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return categoryService.searchCategoriesByName(namePattern, pageIndex, pageSize);
    }

    @Query("categoriesWithProducts")
    @Description("Get categories that have products")
    public List<Category> getCategoriesWithProducts() {
        return categoryService.getCategoriesWithProducts();
    }

    @Query("categoriesWithoutProducts")
    @Description("Get categories that don't have products")
    public List<Category> getCategoriesWithoutProducts() {
        return categoryService.getCategoriesWithoutProducts();
    }

    @Query("categoryStatistics")
    @Description("Get category statistics")
    public CategoryService.CategoryStatistics getCategoryStatistics() {
        return categoryService.getCategoryStatistics();
    }

    // Mutations

    @Mutation("createCategory")
    @Description("Create a new category")
    public Category createCategory(@Name("input") @Valid CategoryInput input) {
        try {
            Category category = new Category();
            category.name = input.name;
            category.description = input.description;
            category.slug = input.slug;
            category.imageUrl = input.imageUrl;
            category.active = input.active;
            category.sortOrder = input.sortOrder;

            if (input.parentId != null) {
                return categoryService.createCategory(category, input.parentId);
            } else {
                return categoryService.createCategory(category);
            }
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("updateCategory")
    @Description("Update an existing category")
    public Category updateCategory(@Name("id") Long id, @Name("input") @Valid CategoryInput input) {
        Category categoryData = new Category();
        categoryData.name = input.name;
        categoryData.description = input.description;
        categoryData.slug = input.slug;
        categoryData.imageUrl = input.imageUrl;
        categoryData.sortOrder = input.sortOrder;
        
        return categoryService.updateCategory(id, categoryData);
    }

    @Mutation("moveCategory")
    @Description("Move category to different parent")
    public Category moveCategory(@Name("categoryId") Long categoryId, @Name("newParentId") Long newParentId) {
        return categoryService.moveCategory(categoryId, newParentId);
    }

    @Mutation("activateCategory")
    @Description("Activate a category")
    public Category activateCategory(@Name("id") Long id) {
        return categoryService.activateCategory(id);
    }

    @Mutation("deactivateCategory")
    @Description("Deactivate a category")
    public Category deactivateCategory(@Name("id") Long id) {
        return categoryService.deactivateCategory(id);
    }

    @Mutation("updateCategorySortOrder")
    @Description("Update category sort order")
    public Category updateCategorySortOrder(@Name("id") Long id, @Name("sortOrder") Integer sortOrder) {
        return categoryService.updateSortOrder(id, sortOrder);
    }

    @Mutation("deleteCategory")
    @Description("Delete a category (soft delete)")
    public Boolean deleteCategory(@Name("id") Long id) {
        categoryService.deleteCategory(id);
        return true;
    }
}
