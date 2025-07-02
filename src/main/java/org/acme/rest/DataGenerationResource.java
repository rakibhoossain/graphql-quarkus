package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.service.DataGenerationService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST endpoint for generating test data
 */
@Path("/api/data-generation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataGenerationResource {

    @Inject
    DataGenerationService dataGenerationService;

    /**
     * Generate 50,000 products with brands and categories
     */
    @POST
    @Path("/generate-50k")
    public Response generate50KProducts() {
        try {
            // Run data generation asynchronously to avoid timeout
            CompletableFuture.runAsync(() -> {
                try {
                    dataGenerationService.generate50KProducts();
                } catch (Exception e) {
                    System.err.println("Error generating data: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return Response.ok(Map.of(
                "message", "Data generation started",
                "status", "in_progress",
                "description", "Generating 100 brands, 50 categories, and 50,000 products",
                "note", "This process will take several minutes. Check the server logs for progress."
            )).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Failed to start data generation",
                    "message", e.getMessage()
                )).build();
        }
    }

    /**
     * Generate custom amount of test data
     */
    @POST
    @Path("/generate-custom")
    public Response generateCustomData(
            @QueryParam("brands") @DefaultValue("10") int numBrands,
            @QueryParam("categories") @DefaultValue("10") int numCategories,
            @QueryParam("products") @DefaultValue("1000") int numProducts) {
        
        try {
            // Validate input
            if (numBrands < 1 || numCategories < 1 || numProducts < 1) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "All counts must be positive numbers")).build();
            }

            if (numProducts > 100000) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Maximum 100,000 products allowed")).build();
            }

            // Run data generation asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    dataGenerationService.generateTestData(numBrands, numCategories, numProducts);
                } catch (Exception e) {
                    System.err.println("Error generating custom data: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return Response.ok(Map.of(
                "message", "Custom data generation started",
                "status", "in_progress",
                "brands", numBrands,
                "categories", numCategories,
                "products", numProducts,
                "note", "Check the server logs for progress."
            )).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Failed to start custom data generation",
                    "message", e.getMessage()
                )).build();
        }
    }

    /**
     * Get current database statistics
     */
    @GET
    @Path("/stats")
    public Response getDatabaseStats() {
        try {
            // You can inject EntityManager or use repositories to get counts
            return Response.ok(Map.of(
                "message", "Use GraphQL queries to check current data counts",
                "queries", Map.of(
                    "brands", "query { brands { id name } }",
                    "categories", "query { categories { id name } }",
                    "products", "query { products { id name } }"
                )
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Failed to get database stats",
                    "message", e.getMessage()
                )).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
            "status", "healthy",
            "service", "Data Generation Service",
            "endpoints", Map.of(
                "generate_50k", "POST /api/data-generation/generate-50k",
                "generate_custom", "POST /api/data-generation/generate-custom?brands=10&categories=10&products=1000",
                "stats", "GET /api/data-generation/stats"
            )
        )).build();
    }
}
