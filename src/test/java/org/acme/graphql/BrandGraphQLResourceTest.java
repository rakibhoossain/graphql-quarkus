package org.acme.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.acme.entity.Brand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BrandGraphQLResourceTest {

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data in correct order
        org.acme.entity.Product.deleteAll();
        org.acme.entity.Category.deleteAll();
        Brand.deleteAll();
    }

    @Test
    void testCreateBrandMutation() {
        String mutation = """
            mutation {
                createBrand(input: {
                    name: "Test Brand"
                    description: "Test Description"
                    logoUrl: "https://example.com/logo.png"
                    websiteUrl: "https://example.com"
                    active: true
                }) {
                    id
                    name
                    description
                    logoUrl
                    websiteUrl
                    active
                    createdAt
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.createBrand.name", equalTo("Test Brand"))
            .body("data.createBrand.description", equalTo("Test Description"))
            .body("data.createBrand.logoUrl", equalTo("https://example.com/logo.png"))
            .body("data.createBrand.websiteUrl", equalTo("https://example.com"))
            .body("data.createBrand.active", equalTo(true))
            .body("data.createBrand.id", notNullValue())
            .body("data.createBrand.createdAt", notNullValue());
    }

    @Test
    void testGetBrandsQuery() {
        // First create a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        String query = """
            query {
                brands {
                    id
                    name
                    description
                    active
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.brands", hasSize(1))
            .body("data.brands[0].name", equalTo("Test Brand"))
            .body("data.brands[0].description", equalTo("Test Description"))
            .body("data.brands[0].active", equalTo(true));
    }

    @Test
    void testGetBrandByIdQuery() {
        // First create a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        String query = """
            query {
                brand(id: %d) {
                    id
                    name
                    description
                    active
                }
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.brand.id", equalTo(testBrand.id.intValue()))
            .body("data.brand.name", equalTo("Test Brand"))
            .body("data.brand.description", equalTo("Test Description"))
            .body("data.brand.active", equalTo(true));
    }

    @Test
    void testSearchBrandsQuery() {
        // Create test brands
        Brand brand1 = new Brand("Apple Inc", "Technology company");
        brand1.persist();
        Brand brand2 = new Brand("Samsung Electronics", "Electronics company");
        brand2.persist();
        Brand brand3 = new Brand("Nike", "Sports company");
        brand3.persist();

        String query = """
            query {
                searchBrands(namePattern: "Apple") {
                    id
                    name
                    description
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.searchBrands", hasSize(1))
            .body("data.searchBrands[0].name", equalTo("Apple Inc"));
    }

    @Test
    void testUpdateBrandMutation() {
        // First create a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        String mutation = """
            mutation {
                updateBrand(id: %d, input: {
                    name: "Updated Brand"
                    description: "Updated Description"
                    logoUrl: "https://example.com/new-logo.png"
                }) {
                    id
                    name
                    description
                    logoUrl
                }
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.updateBrand.id", equalTo(testBrand.id.intValue()))
            .body("data.updateBrand.name", equalTo("Updated Brand"))
            .body("data.updateBrand.description", equalTo("Updated Description"))
            .body("data.updateBrand.logoUrl", equalTo("https://example.com/new-logo.png"));
    }

    @Test
    void testActivateBrandMutation() {
        // First create and deactivate a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.active = false;
        testBrand.persist();

        String mutation = """
            mutation {
                activateBrand(id: %d) {
                    id
                    active
                }
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.activateBrand.id", equalTo(testBrand.id.intValue()))
            .body("data.activateBrand.active", equalTo(true));
    }

    @Test
    void testDeactivateBrandMutation() {
        // First create a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        String mutation = """
            mutation {
                deactivateBrand(id: %d) {
                    id
                    active
                }
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.deactivateBrand.id", equalTo(testBrand.id.intValue()))
            .body("data.deactivateBrand.active", equalTo(false));
    }

    @Test
    void testDeleteBrandMutation() {
        // First create a brand
        Brand testBrand = new Brand("Test Brand", "Test Description");
        testBrand.persist();

        String mutation = """
            mutation {
                deleteBrand(id: %d)
            }
            """.formatted(testBrand.id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.deleteBrand", equalTo(true));
    }

    @Test
    void testGetBrandStatisticsQuery() {
        // Create test brands
        Brand activeBrand = new Brand("Active Brand", "Active Description");
        activeBrand.persist();
        
        Brand inactiveBrand = new Brand("Inactive Brand", "Inactive Description");
        inactiveBrand.active = false;
        inactiveBrand.persist();

        String query = """
            query {
                brandStatistics {
                    totalActive
                    totalWithProducts
                    totalWithoutProducts
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body("{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("data.brandStatistics.totalActive", equalTo(1))
            .body("data.brandStatistics.totalWithProducts", equalTo(0))
            .body("data.brandStatistics.totalWithoutProducts", equalTo(1));
    }

    @Test
    void testCreateBrandWithValidationError() {
        String mutation = """
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
            .body("{\"query\":\"" + mutation.replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
            .when()
            .post("/graphql")
            .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("errors[0].message", containsString("Brand name is required"));
    }
}
