package org.acme.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Brand;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BrandServiceTest {

    @Inject
    BrandService brandService;

    private Brand testBrand;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data in correct order (products first, then categories, then brands)
        org.acme.entity.Product.deleteAll();
        org.acme.entity.Category.deleteAll();
        Brand.deleteAll();

        // Create test brand
        testBrand = new Brand("Test Brand", "Test Description");
        testBrand.logoUrl = "https://example.com/logo.png";
        testBrand.websiteUrl = "https://example.com";
    }

    @Test
    @Transactional
    void testCreateBrand() {
        // When
        Brand createdBrand = brandService.createBrand(testBrand);

        // Then
        assertNotNull(createdBrand);
        assertNotNull(createdBrand.id);
        assertEquals("Test Brand", createdBrand.name);
        assertEquals("Test Description", createdBrand.description);
        assertTrue(createdBrand.active);
        assertNotNull(createdBrand.createdAt);
        assertNotNull(createdBrand.updatedAt);
    }

    @Test
    @Transactional
    void testCreateBrandWithDuplicateName() {
        // Given
        brandService.createBrand(testBrand);

        // When & Then
        Brand duplicateBrand = new Brand("Test Brand", "Another Description");
        assertThrows(BusinessException.class, () -> brandService.createBrand(duplicateBrand));
    }

    @Test
    @Transactional
    void testFindBrandById() {
        // Given
        Brand createdBrand = brandService.createBrand(testBrand);

        // When
        Brand foundBrand = brandService.findBrandById(createdBrand.id);

        // Then
        assertNotNull(foundBrand);
        assertEquals(createdBrand.id, foundBrand.id);
        assertEquals("Test Brand", foundBrand.name);
    }

    @Test
    void testFindBrandByIdNotFound() {
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> brandService.findBrandById(999L));
    }

    @Test
    @Transactional
    void testFindBrandByName() {
        // Given
        brandService.createBrand(testBrand);

        // When
        Optional<Brand> foundBrand = brandService.findBrandByName("Test Brand");

        // Then
        assertTrue(foundBrand.isPresent());
        assertEquals("Test Brand", foundBrand.get().name);
    }

    @Test
    @Transactional
    void testFindBrandByNameNotFound() {
        // When
        Optional<Brand> foundBrand = brandService.findBrandByName("Non-existent Brand");

        // Then
        assertFalse(foundBrand.isPresent());
    }

    @Test
    @Transactional
    void testUpdateBrand() {
        // Given
        Brand createdBrand = brandService.createBrand(testBrand);
        
        Brand updateData = new Brand();
        updateData.name = "Updated Brand Name";
        updateData.description = "Updated Description";
        updateData.logoUrl = "https://example.com/new-logo.png";

        // When
        Brand updatedBrand = brandService.updateBrand(createdBrand.id, updateData);

        // Then
        assertNotNull(updatedBrand);
        assertEquals("Updated Brand Name", updatedBrand.name);
        assertEquals("Updated Description", updatedBrand.description);
        assertEquals("https://example.com/new-logo.png", updatedBrand.logoUrl);
    }

    @Test
    @Transactional
    void testUpdateBrandWithDuplicateName() {
        // Given
        Brand brand1 = brandService.createBrand(testBrand);
        Brand brand2 = brandService.createBrand(new Brand("Another Brand", "Another Description"));
        
        Brand updateData = new Brand();
        updateData.name = "Test Brand"; // Same as brand1

        // When & Then
        assertThrows(BusinessException.class, () -> brandService.updateBrand(brand2.id, updateData));
    }

    @Test
    @Transactional
    void testActivateBrand() {
        // Given
        Brand createdBrand = brandService.createBrand(testBrand);
        createdBrand.deactivate();
        Brand.persist(createdBrand);

        // When
        Brand activatedBrand = brandService.activateBrand(createdBrand.id);

        // Then
        assertTrue(activatedBrand.active);
    }

    @Test
    @Transactional
    void testDeactivateBrand() {
        // Given
        Brand createdBrand = brandService.createBrand(testBrand);

        // When
        Brand deactivatedBrand = brandService.deactivateBrand(createdBrand.id);

        // Then
        assertFalse(deactivatedBrand.active);
    }

    @Test
    @Transactional
    void testDeleteBrand() {
        // Given
        Brand createdBrand = brandService.createBrand(testBrand);

        // When
        brandService.deleteBrand(createdBrand.id);

        // Then
        Brand foundBrand = brandService.findBrandById(createdBrand.id);
        assertFalse(foundBrand.active);
    }

    @Test
    @Transactional
    void testGetAllActiveBrands() {
        // Given
        brandService.createBrand(testBrand);
        Brand inactiveBrand = new Brand("Inactive Brand", "Inactive Description");
        inactiveBrand.active = false;
        brandService.createBrand(inactiveBrand);

        // When
        List<Brand> activeBrands = brandService.getAllActiveBrands();

        // Then
        assertEquals(1, activeBrands.size());
        assertEquals("Test Brand", activeBrands.get(0).name);
    }

    @Test
    @Transactional
    void testSearchBrandsByName() {
        // Given
        brandService.createBrand(testBrand);
        brandService.createBrand(new Brand("Another Test Brand", "Another Description"));
        brandService.createBrand(new Brand("Different Brand", "Different Description"));

        // When
        List<Brand> searchResults = brandService.searchBrandsByName("Test");

        // Then
        assertEquals(2, searchResults.size());
        assertTrue(searchResults.stream().allMatch(brand -> brand.name.contains("Test")));
    }

    @Test
    @Transactional
    void testGetBrandStatistics() {
        // Given
        brandService.createBrand(testBrand);
        Brand inactiveBrand = new Brand("Inactive Brand", "Inactive Description");
        inactiveBrand.active = false;
        brandService.createBrand(inactiveBrand);

        // When
        BrandService.BrandStatistics stats = brandService.getBrandStatistics();

        // Then
        assertEquals(1, stats.totalActive);
        assertEquals(0, stats.totalWithProducts); // No products created in this test
        assertEquals(1, stats.totalWithoutProducts);
    }

    @Test
    @Transactional
    void testBulkActivateBrands() {
        // Given
        Brand brand1 = brandService.createBrand(testBrand);
        Brand brand2 = brandService.createBrand(new Brand("Brand 2", "Description 2"));
        
        // Deactivate both brands
        brandService.deactivateBrand(brand1.id);
        brandService.deactivateBrand(brand2.id);

        // When
        int updatedCount = brandService.activateBrands(List.of(brand1.id, brand2.id));

        // Then
        assertEquals(2, updatedCount);
        assertTrue(brandService.findBrandById(brand1.id).active);
        assertTrue(brandService.findBrandById(brand2.id).active);
    }

    @Test
    @Transactional
    void testBulkDeactivateBrands() {
        // Given
        Brand brand1 = brandService.createBrand(testBrand);
        Brand brand2 = brandService.createBrand(new Brand("Brand 2", "Description 2"));

        // When
        int updatedCount = brandService.deactivateBrands(List.of(brand1.id, brand2.id));

        // Then
        assertEquals(2, updatedCount);
        assertFalse(brandService.findBrandById(brand1.id).active);
        assertFalse(brandService.findBrandById(brand2.id).active);
    }
}
