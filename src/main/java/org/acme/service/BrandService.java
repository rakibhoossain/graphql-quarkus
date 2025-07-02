package org.acme.service;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.acme.entity.Brand;
import org.acme.repository.BrandRepository;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Service for Brand business logic operations
 */
@ApplicationScoped
public class BrandService {

    @Inject
    BrandRepository brandRepository;

    /**
     * Create a new brand
     */
    @Transactional
    public Brand createBrand(@Valid @NotNull Brand brand) {
        validateBrandForCreation(brand);
        brandRepository.persist(brand);
        return brand;
    }

    /**
     * Update an existing brand
     */
    @Transactional
    public Brand updateBrand(@NotNull Long brandId, @Valid @NotNull Brand brandData) {
        Brand existingBrand = findBrandById(brandId);
        validateBrandForUpdate(brandData, brandId);
        
        // Update fields
        existingBrand.name = brandData.name;
        existingBrand.description = brandData.description;
        existingBrand.logoUrl = brandData.logoUrl;
        existingBrand.websiteUrl = brandData.websiteUrl;
        
        brandRepository.persist(existingBrand);
        return existingBrand;
    }

    /**
     * Find brand by ID
     */
    public Brand findBrandById(@NotNull Long brandId) {
        return brandRepository.findByIdOptional(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with ID: " + brandId));
    }

    /**
     * Find brand by name
     */
    public Optional<Brand> findBrandByName(@NotNull String name) {
        return brandRepository.findByName(name);
    }

    /**
     * Get all active brands
     */
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findAllActive();
    }

    /**
     * Get all active brands with pagination
     */
    public List<Brand> getAllActiveBrands(int pageIndex, int pageSize) {
        return brandRepository.findAllActive(Page.of(pageIndex, pageSize));
    }

    /**
     * Search brands by name pattern
     */
    public List<Brand> searchBrandsByName(@NotNull String namePattern) {
        return brandRepository.findByNameContaining(namePattern);
    }

    /**
     * Search brands by name pattern with pagination
     */
    public List<Brand> searchBrandsByName(@NotNull String namePattern, int pageIndex, int pageSize) {
        return brandRepository.findByNameContaining(namePattern, Page.of(pageIndex, pageSize));
    }

    /**
     * Get brands with products
     */
    public List<Brand> getBrandsWithProducts() {
        return brandRepository.findBrandsWithProducts();
    }

    /**
     * Get brands without products
     */
    public List<Brand> getBrandsWithoutProducts() {
        return brandRepository.findBrandsWithoutProducts();
    }

    /**
     * Activate brand
     */
    @Transactional
    public Brand activateBrand(@NotNull Long brandId) {
        Brand brand = findBrandById(brandId);
        brand.activate();
        brandRepository.persist(brand);
        return brand;
    }

    /**
     * Deactivate brand
     */
    @Transactional
    public Brand deactivateBrand(@NotNull Long brandId) {
        Brand brand = findBrandById(brandId);
        brand.deactivate();
        brandRepository.persist(brand);
        return brand;
    }

    /**
     * Soft delete brand (deactivate)
     */
    @Transactional
    public void deleteBrand(@NotNull Long brandId) {
        Brand brand = findBrandById(brandId);
        brand.deactivate();
        brandRepository.persist(brand);
    }

    /**
     * Bulk activate brands
     */
    @Transactional
    public int activateBrands(@NotNull List<Long> brandIds) {
        validateBrandIds(brandIds);
        return brandRepository.activateBrands(brandIds);
    }

    /**
     * Bulk deactivate brands
     */
    @Transactional
    public int deactivateBrands(@NotNull List<Long> brandIds) {
        validateBrandIds(brandIds);
        return brandRepository.deactivateBrands(brandIds);
    }

    /**
     * Get brand statistics
     */
    public BrandStatistics getBrandStatistics() {
        long totalActive = brandRepository.countActive();
        long totalWithProducts = brandRepository.findBrandsWithProducts().size();
        long totalWithoutProducts = brandRepository.findBrandsWithoutProducts().size();
        
        return new BrandStatistics(totalActive, totalWithProducts, totalWithoutProducts);
    }

    /**
     * Get recently created brands
     */
    public List<Brand> getRecentlyCreatedBrands(int limit) {
        return brandRepository.findRecentlyCreated(limit);
    }

    /**
     * Get recently updated brands
     */
    public List<Brand> getRecentlyUpdatedBrands(int limit) {
        return brandRepository.findRecentlyUpdated(limit);
    }

    // Validation methods
    private void validateBrandForCreation(Brand brand) {
        if (brandRepository.existsByName(brand.name)) {
            throw new BusinessException("Brand with name '" + brand.name + "' already exists");
        }
    }

    private void validateBrandForUpdate(Brand brand, Long brandId) {
        if (brandRepository.existsByNameExcludingId(brand.name, brandId)) {
            throw new BusinessException("Brand with name '" + brand.name + "' already exists");
        }
    }

    private void validateBrandIds(List<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            throw new BusinessException("Brand IDs list cannot be null or empty");
        }
    }

    /**
     * Brand statistics data class
     */
    public static class BrandStatistics {
        public final long totalActive;
        public final long totalWithProducts;
        public final long totalWithoutProducts;

        public BrandStatistics(long totalActive, long totalWithProducts, long totalWithoutProducts) {
            this.totalActive = totalActive;
            this.totalWithProducts = totalWithProducts;
            this.totalWithoutProducts = totalWithoutProducts;
        }
    }
}
