package org.acme.graphql;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.acme.entity.Brand;
import org.acme.graphql.exception.ExceptionMapper;
import org.acme.graphql.input.BrandInput;
import org.acme.service.BrandService;
import org.eclipse.microprofile.graphql.*;

import java.util.List;
import java.util.Optional;

/**
 * GraphQL API for Brand operations
 */
@GraphQLApi
public class BrandGraphQLResource {

    @Inject
    BrandService brandService;

    @Inject
    ExceptionMapper exceptionMapper;

    // Queries
    
    @Query("brand")
    @Description("Get a brand by ID")
    public Brand getBrand(@Name("id") Long id) {
        return brandService.findBrandById(id);
    }

    @Query("brandByName")
    @Description("Get a brand by name")
    public Optional<Brand> getBrandByName(@Name("name") String name) {
        return brandService.findBrandByName(name);
    }

    @Query("brands")
    @Description("Get all active brands")
    public List<Brand> getAllBrands() {
        return brandService.getAllActiveBrands();
    }

    @Query("brandsWithPagination")
    @Description("Get all active brands with pagination")
    public List<Brand> getAllBrandsWithPagination(
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return brandService.getAllActiveBrands(pageIndex, pageSize);
    }

    @Query("searchBrands")
    @Description("Search brands by name pattern")
    public List<Brand> searchBrands(@Name("namePattern") String namePattern) {
        return brandService.searchBrandsByName(namePattern);
    }

    @Query("searchBrandsWithPagination")
    @Description("Search brands by name pattern with pagination")
    public List<Brand> searchBrandsWithPagination(
            @Name("namePattern") String namePattern,
            @Name("pageIndex") @DefaultValue("0") int pageIndex,
            @Name("pageSize") @DefaultValue("20") int pageSize) {
        return brandService.searchBrandsByName(namePattern, pageIndex, pageSize);
    }

    @Query("brandsWithProducts")
    @Description("Get brands that have products")
    public List<Brand> getBrandsWithProducts() {
        return brandService.getBrandsWithProducts();
    }

    @Query("brandsWithoutProducts")
    @Description("Get brands that don't have products")
    public List<Brand> getBrandsWithoutProducts() {
        return brandService.getBrandsWithoutProducts();
    }

    @Query("brandStatistics")
    @Description("Get brand statistics")
    public BrandService.BrandStatistics getBrandStatistics() {
        return brandService.getBrandStatistics();
    }

    @Query("recentlyCreatedBrands")
    @Description("Get recently created brands")
    public List<Brand> getRecentlyCreatedBrands(@Name("limit") @DefaultValue("10") int limit) {
        return brandService.getRecentlyCreatedBrands(limit);
    }

    @Query("recentlyUpdatedBrands")
    @Description("Get recently updated brands")
    public List<Brand> getRecentlyUpdatedBrands(@Name("limit") @DefaultValue("10") int limit) {
        return brandService.getRecentlyUpdatedBrands(limit);
    }

    // Mutations

    @Mutation("createBrand")
    @Description("Create a new brand")
    public Brand createBrand(@Name("input") @Valid BrandInput input) {
        try {
            Brand brand = new Brand();
            brand.name = input.name;
            brand.description = input.description;
            brand.logoUrl = input.logoUrl;
            brand.websiteUrl = input.websiteUrl;
            brand.active = input.active;

            return brandService.createBrand(brand);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("updateBrand")
    @Description("Update an existing brand")
    public Brand updateBrand(@Name("id") Long id, @Name("input") @Valid BrandInput input) {
        try {
            Brand brandData = new Brand();
            brandData.name = input.name;
            brandData.description = input.description;
            brandData.logoUrl = input.logoUrl;
            brandData.websiteUrl = input.websiteUrl;

            return brandService.updateBrand(id, brandData);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("activateBrand")
    @Description("Activate a brand")
    public Brand activateBrand(@Name("id") Long id) {
        try {
            return brandService.activateBrand(id);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("deactivateBrand")
    @Description("Deactivate a brand")
    public Brand deactivateBrand(@Name("id") Long id) {
        try {
            return brandService.deactivateBrand(id);
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("deleteBrand")
    @Description("Delete a brand (soft delete)")
    public Boolean deleteBrand(@Name("id") Long id) {
        try {
            brandService.deleteBrand(id);
            return true;
        } catch (Exception e) {
            throw exceptionMapper.mapToGraphQLException(e);
        }
    }

    @Mutation("activateBrands")
    @Description("Bulk activate brands")
    public Integer activateBrands(@Name("ids") List<Long> ids) {
        return brandService.activateBrands(ids);
    }

    @Mutation("deactivateBrands")
    @Description("Bulk deactivate brands")
    public Integer deactivateBrands(@Name("ids") List<Long> ids) {
        return brandService.deactivateBrands(ids);
    }
}
