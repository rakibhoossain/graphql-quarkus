package org.acme.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ErrorHandlingTest {

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        Product.deleteAll();
        Category.deleteAll();
        Brand.deleteAll();
    }

    @Test
    void testCreateBrandWithDuplicateName() {
        // First create a brand
        String createBrandMutation = """
            mutation {
                createBrand(input: {
                    name: "Test Brand"
                    description: "Test Description"
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createBrandMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createBrand.name", equalTo("Test Brand"));

        // Try to create another brand with the same name
        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createBrandMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("already exists"))
            .body("errors[0].extensions.classification", equalTo("BUSINESS_ERROR"))
            .body("errors[0].extensions.code", equalTo("DUPLICATE_ENTITY"));
    }

    @Test
    void testCreateBrandWithValidationError() {
        String createBrandMutation = """
            mutation {
                createBrand(input: {
                    name: ""
                    description: "Test Description"
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createBrandMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("Brand name is required"));
    }

    @Test
    void testUpdateNonExistentBrand() {
        String updateBrandMutation = """
            mutation {
                updateBrand(id: 999, input: {
                    name: "Updated Brand"
                    description: "Updated Description"
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + updateBrandMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("not found"))
            .body("errors[0].extensions.classification", equalTo("BUSINESS_ERROR"))
            .body("errors[0].extensions.code", equalTo("ENTITY_NOT_FOUND"));
    }

    @Test
    void testCreateProductWithInvalidPrice() {
        String createProductMutation = """
            mutation {
                createProduct(input: {
                    name: "Test Product"
                    description: "Test Description"
                    price: -10.00
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProductMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("Price must be greater than 0"));
    }

    @Test
    void testCreateProductWithDuplicateSKU() {
        // First create a product
        String createProductMutation1 = """
            mutation {
                createProduct(input: {
                    name: "Test Product 1"
                    description: "Test Description 1"
                    sku: "TEST-SKU-001"
                    price: 99.99
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProductMutation1.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createProduct.name", equalTo("Test Product 1"));

        // Try to create another product with the same SKU
        String createProductMutation2 = """
            mutation {
                createProduct(input: {
                    name: "Test Product 2"
                    description: "Test Description 2"
                    sku: "TEST-SKU-001"
                    price: 149.99
                }) {
                    id
                    name
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProductMutation2.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("SKU"))
            .body("errors[0].message", containsString("already exists"))
            .body("errors[0].extensions.classification", equalTo("BUSINESS_ERROR"))
            .body("errors[0].extensions.code", equalTo("DUPLICATE_ENTITY"));
    }

    @Test
    void testReduceStockBeyondAvailable() {
        // First create a brand and category
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        Category testCategory = new Category("Test Category", "Test Description");
        testCategory.persist();

        // Create a product with limited stock
        String createProductMutation = """
            mutation {
                createProduct(input: {
                    name: "Limited Stock Product"
                    description: "Product with limited stock"
                    sku: "LIMITED-001"
                    price: 99.99
                    stockQuantity: 5
                    brandId: %d
                    categoryId: %d
                }) {
                    id
                    name
                    stockQuantity
                }
            }
            """.formatted(testBrand.id, testCategory.id);

        Integer productId = given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createProductMutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createProduct.stockQuantity", equalTo(5))
            .extract()
            .path("data.createProduct.id");

        // Try to reduce stock beyond available quantity
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
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("Insufficient stock"))
            .body("errors[0].extensions.classification", equalTo("BUSINESS_ERROR"))
            .body("errors[0].extensions.code", equalTo("INSUFFICIENT_STOCK"));
    }

    @Test
    void testCreateCategoryWithDuplicateSlug() {
        // First create a category
        String createCategoryMutation1 = """
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

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createCategoryMutation1.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createCategory.slug", equalTo("electronics"));

        // Try to create another category with the same slug
        String createCategoryMutation2 = """
            mutation {
                createCategory(input: {
                    name: "Electronic Devices"
                    description: "Another electronics category"
                    slug: "electronics"
                }) {
                    id
                    name
                    slug
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + createCategoryMutation2.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("slug"))
            .body("errors[0].message", containsString("already exists"))
            .body("errors[0].extensions.classification", equalTo("BUSINESS_ERROR"))
            .body("errors[0].extensions.code", equalTo("DUPLICATE_ENTITY"));
    }
}
