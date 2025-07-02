package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;
import org.acme.service.BrandService;
import org.acme.service.CategoryService;
import org.acme.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EcommerceIntegrationTest {

    @Inject
    BrandService brandService;

    @Inject
    CategoryService categoryService;

    @Inject
    ProductService productService;

    private Brand testBrand;
    private Category testCategory;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data in correct order
        Product.deleteAll();
        Category.deleteAll();
        Brand.deleteAll();

        // Create test data
        testBrand = new Brand("Test Brand", "Test Brand Description");
        testBrand = brandService.createBrand(testBrand);

        testCategory = new Category("Test Category", "Test Category Description");
        testCategory = categoryService.createCategory(testCategory);
    }

    @Test
    void testCompleteEcommerceWorkflow() {
        // Test creating a complete product with brand and category
        String createProductMutation = """
            mutation {
                createProduct(input: {
                    name: "Test Product"
                    description: "Test Product Description"
                    sku: "TEST-PROD-001"
                    price: 99.99
                    stockQuantity: 50
                    brandId: %d
                    categoryId: %d
                    featured: true
                    trackInventory: true
                }) {
                    id
                    name
                    description
                    sku
                    price
                    stockQuantity
                    featured
                    brand {
                        id
                        name
                    }
                    category {
                        id
                        name
                    }
                }
            }
            """.formatted(testBrand.id, testCategory.id);

        // Create product via GraphQL
        Integer productIdInt = given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProductMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createProduct.name", equalTo("Test Product"))
            .body("data.createProduct.sku", equalTo("TEST-PROD-001"))
            .body("data.createProduct.price", equalTo(99.99f))
            .body("data.createProduct.stockQuantity", equalTo(50))
            .body("data.createProduct.featured", equalTo(true))
            .body("data.createProduct.brand.name", equalTo("Test Brand"))
            .body("data.createProduct.category.name", equalTo("Test Category"))
            .extract()
            .path("data.createProduct.id");

        Long productId = productIdInt.longValue();

        // Test querying products by category
        String productsByCategoryQuery = """
            query {
                productsByCategory(categoryId: %d) {
                    id
                    name
                    brand {
                        name
                    }
                    category {
                        name
                    }
                }
            }
            """.formatted(testCategory.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + productsByCategoryQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.productsByCategory", hasSize(1))
            .body("data.productsByCategory[0].name", equalTo("Test Product"));

        // Test querying products by brand
        String productsByBrandQuery = """
            query {
                productsByBrand(brandId: %d) {
                    id
                    name
                    brand {
                        name
                    }
                }
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + productsByBrandQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.productsByBrand", hasSize(1))
            .body("data.productsByBrand[0].name", equalTo("Test Product"));

        // Test updating stock
        String updateStockMutation = """
            mutation {
                updateProductStock(id: %d, quantity: 25) {
                    id
                    stockQuantity
                }
            }
            """.formatted(productId);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + updateStockMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.updateProductStock.stockQuantity", equalTo(25));

        // Test reducing stock
        String reduceStockMutation = """
            mutation {
                reduceProductStock(id: %d, quantity: 10) {
                    id
                    stockQuantity
                }
            }
            """.formatted(productId);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + reduceStockMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.reduceProductStock.stockQuantity", equalTo(15));

        // Test setting featured status
        String setFeaturedMutation = """
            mutation {
                setProductFeatured(id: %d, featured: false) {
                    id
                    featured
                }
            }
            """.formatted(productId);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + setFeaturedMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.setProductFeatured.featured", equalTo(false));

        // Test getting featured products (should be empty now)
        String featuredProductsQuery = """
            query {
                featuredProducts {
                    id
                    name
                    featured
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + featuredProductsQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.featuredProducts", hasSize(0));
    }

    @Test
    void testCategoryHierarchy() {
        // Create parent and child categories
        String createParentCategoryMutation = """
            mutation {
                createCategory(input: {
                    name: "Electronics"
                    description: "Electronic devices"
                    slug: "electronics"
                }) {
                    id
                    name
                    slug
                }
            }
            """;

        Integer parentCategoryIdInt = given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createParentCategoryMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createCategory.name", equalTo("Electronics"))
            .extract()
            .path("data.createCategory.id");

        Long parentCategoryId = parentCategoryIdInt.longValue();

        String createChildCategoryMutation = """
            mutation {
                createCategory(input: {
                    name: "Smartphones"
                    description: "Mobile phones"
                    slug: "smartphones"
                    parentId: %d
                }) {
                    id
                    name
                    slug
                    parent {
                        id
                        name
                    }
                }
            }
            """.formatted(parentCategoryId);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createChildCategoryMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createCategory.name", equalTo("Smartphones"))
            .body("data.createCategory.parent.name", equalTo("Electronics"));

        // Test getting root categories
        String rootCategoriesQuery = """
            query {
                rootCategories {
                    id
                    name
                    children {
                        id
                        name
                    }
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + rootCategoriesQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.rootCategories", hasSize(greaterThan(0)))
            .body("data.rootCategories.find { it.name == 'Electronics' }.children", hasSize(1))
            .body("data.rootCategories.find { it.name == 'Electronics' }.children[0].name", equalTo("Smartphones"));
    }

    @Test
    @Transactional
    void testServiceLayerIntegration() {
        // Test creating entities through service layer
        Brand serviceBrand = new Brand("Service Brand", "Created via service");
        serviceBrand = brandService.createBrand(serviceBrand);

        Category serviceCategory = new Category("Service Category", "Created via service");
        serviceCategory = categoryService.createCategory(serviceCategory);

        Product serviceProduct = new Product("Service Product", new BigDecimal("199.99"));
        serviceProduct.description = "Created via service";
        serviceProduct.sku = "SERVICE-PROD-001";
        serviceProduct.category = serviceCategory;
        serviceProduct.brand = serviceBrand;
        serviceProduct.stockQuantity = 100;
        serviceProduct = productService.createProduct(serviceProduct);

        // Verify relationships
        assertNotNull(serviceProduct.id);
        assertEquals("Service Product", serviceProduct.name);
        assertEquals(serviceBrand.id, serviceProduct.brand.id);
        assertEquals(serviceCategory.id, serviceProduct.category.id);

        // Test business logic
        assertTrue(serviceProduct.isInStock());
        assertFalse(serviceProduct.isLowStock());

        // Test stock operations
        productService.reduceStock(serviceProduct.id, 95);
        Product updatedProduct = productService.findProductById(serviceProduct.id);
        assertEquals(5, updatedProduct.stockQuantity);
        assertTrue(updatedProduct.isLowStock());

        productService.addStock(serviceProduct.id, 20);
        updatedProduct = productService.findProductById(serviceProduct.id);
        assertEquals(25, updatedProduct.stockQuantity);
        assertFalse(updatedProduct.isLowStock());
    }

    @Test
    void testSearchFunctionality() {
        // Create multiple products for search testing
        String createProduct1Mutation = """
            mutation {
                createProduct(input: {
                    name: "iPhone 15 Pro"
                    description: "Latest iPhone model"
                    sku: "IPHONE-15-PRO"
                    price: 999.99
                    brandId: %d
                    categoryId: %d
                }) {
                    id
                    name
                }
            }
            """.formatted(testBrand.id, testCategory.id);

        String createProduct2Mutation = """
            mutation {
                createProduct(input: {
                    name: "Samsung Galaxy S24"
                    description: "Latest Samsung phone"
                    sku: "GALAXY-S24"
                    price: 899.99
                    brandId: %d
                    categoryId: %d
                }) {
                    id
                    name
                }
            }
            """.formatted(testBrand.id, testCategory.id);

        // Create products
        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProduct1Mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql");

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProduct2Mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql");

        // Test search by name pattern
        String searchQuery = """
            query {
                searchProducts(namePattern: "iPhone") {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + searchQuery.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.searchProducts", hasSize(1))
            .body("data.searchProducts[0].name", containsString("iPhone"));
    }
}
