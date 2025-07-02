package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.acme.entity.Brand;
import org.acme.entity.Category;
import org.acme.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for generating large amounts of test data
 */
@ApplicationScoped
public class DataGenerationService {

    @PersistenceContext
    EntityManager entityManager;

    private final Random random = new Random();

    // Sample data arrays for realistic product generation
    private final String[] brandNames = {
        "Apple", "Samsung", "Google", "Microsoft", "Sony", "LG", "Dell", "HP", "Lenovo", "Asus",
        "Nike", "Adidas", "Puma", "Under Armour", "New Balance", "Reebok", "Converse", "Vans",
        "Canon", "Nikon", "Fujifilm", "Olympus", "Panasonic", "GoPro", "DJI", "Garmin",
        "Bose", "JBL", "Beats", "Sennheiser", "Audio-Technica", "Shure", "Yamaha", "Pioneer",
        "Tesla", "BMW", "Mercedes", "Audi", "Toyota", "Honda", "Ford", "Chevrolet",
        "Rolex", "Omega", "Seiko", "Casio", "Citizen", "Tissot", "TAG Heuer", "Breitling"
    };

    private final String[] categoryNames = {
        "Electronics", "Smartphones", "Laptops", "Tablets", "Headphones", "Cameras", "Gaming",
        "Sports", "Running Shoes", "Basketball Shoes", "Athletic Wear", "Fitness Equipment",
        "Home & Garden", "Kitchen Appliances", "Furniture", "Lighting", "Decor", "Tools",
        "Fashion", "Men's Clothing", "Women's Clothing", "Accessories", "Watches", "Jewelry",
        "Books", "Fiction", "Non-Fiction", "Educational", "Children's Books", "Comics",
        "Automotive", "Car Parts", "Motorcycles", "Bicycles", "Car Electronics", "Tires",
        "Health & Beauty", "Skincare", "Makeup", "Hair Care", "Supplements", "Medical Devices"
    };

    private final String[] productAdjectives = {
        "Premium", "Professional", "Advanced", "Ultimate", "Pro", "Elite", "Deluxe", "Superior",
        "High-Performance", "Ultra", "Smart", "Wireless", "Portable", "Compact", "Lightweight",
        "Durable", "Waterproof", "Fast", "Powerful", "Efficient", "Eco-Friendly", "Innovative"
    };

    private final String[] productTypes = {
        "Phone", "Laptop", "Tablet", "Watch", "Headphones", "Speaker", "Camera", "Monitor",
        "Keyboard", "Mouse", "Charger", "Case", "Stand", "Adapter", "Cable", "Battery",
        "Shoes", "Shirt", "Jacket", "Pants", "Dress", "Bag", "Wallet", "Sunglasses",
        "Book", "Magazine", "Journal", "Notebook", "Pen", "Pencil", "Marker", "Eraser"
    };

    /**
     * Generate test data with specified quantities
     */
    public void generateTestData(int numBrands, int numCategories, int numProducts) {
        try {
            System.out.println("Starting data generation...");
            System.out.println("Generating " + numBrands + " brands, " + numCategories + " categories, and " + numProducts + " products");

            // Clear existing data
            clearExistingData();

            // Generate brands
            List<Brand> brands = generateBrands(numBrands);
            System.out.println("Generated " + brands.size() + " brands");

            // Generate categories
            List<Category> categories = generateCategories(numCategories);
            System.out.println("Generated " + categories.size() + " categories");

            // Generate products in batches
            generateProductsInBatches(numProducts, brands, categories);

            System.out.println("Data generation completed!");
        } catch (Exception e) {
            System.err.println("Error during data generation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Data generation failed", e);
        }
    }

    /**
     * Generate 50,000 products with realistic data distribution
     */
    public void generate50KProducts() {
        generateTestData(100, 50, 50000);
    }

    @Transactional
    public void clearExistingData() {
        try {
            System.out.println("Clearing existing data...");

            // Delete in proper order to avoid foreign key constraints
            int deletedProducts = entityManager.createQuery("DELETE FROM Product").executeUpdate();
            System.out.println("Deleted " + deletedProducts + " products");

            int deletedCategories = entityManager.createQuery("DELETE FROM Category").executeUpdate();
            System.out.println("Deleted " + deletedCategories + " categories");

            int deletedBrands = entityManager.createQuery("DELETE FROM Brand").executeUpdate();
            System.out.println("Deleted " + deletedBrands + " brands");

            entityManager.flush();
            System.out.println("Data clearing completed successfully");
        } catch (Exception e) {
            System.err.println("Error clearing existing data: " + e.getMessage());
            throw new RuntimeException("Failed to clear existing data", e);
        }
    }

    @Transactional
    public List<Brand> generateBrands(int count) {
        List<Brand> brands = new ArrayList<>();

        try {
            System.out.println("Generating " + count + " brands...");

            for (int i = 0; i < count; i++) {
                Brand brand = new Brand();

                // Use predefined names for first brands, then generate unique ones
                if (i < brandNames.length) {
                    brand.name = brandNames[i];
                } else {
                    brand.name = generateUniqueBrandName(i);
                }

                brand.description = "Premium brand offering high-quality products with innovative design and technology.";
                brand.logoUrl = "https://example.com/logos/" + brand.name.toLowerCase().replace(" ", "-") + ".png";
                brand.websiteUrl = "https://www." + brand.name.toLowerCase().replace(" ", "") + ".com";
                brand.active = true;
                brand.createdAt = LocalDateTime.now().minusDays(random.nextInt(365));
                brand.updatedAt = brand.createdAt.plusDays(random.nextInt(30));

                entityManager.persist(brand);
                brands.add(brand);

                if (i % 20 == 0 && i > 0) {
                    entityManager.flush();
                    System.out.println("Generated " + (i + 1) + " brands so far...");
                }
            }

            entityManager.flush();
            System.out.println("Successfully generated " + brands.size() + " brands");
            return brands;
        } catch (Exception e) {
            System.err.println("Error generating brands: " + e.getMessage());
            throw new RuntimeException("Failed to generate brands", e);
        }
    }

    @Transactional
    public List<Category> generateCategories(int count) {
        List<Category> categories = new ArrayList<>();

        try {
            System.out.println("Generating " + count + " categories...");

            for (int i = 0; i < count; i++) {
                Category category = new Category();

                // Use predefined names for first categories, then generate unique ones
                if (i < categoryNames.length) {
                    category.name = categoryNames[i];
                } else {
                    category.name = generateUniqueCategoryName(i);
                }

                category.description = "Comprehensive category featuring a wide range of " + category.name.toLowerCase() + " products.";
                category.slug = category.name.toLowerCase().replace(" ", "-").replace("&", "and") + "-" + i;
                category.imageUrl = "https://example.com/categories/" + category.slug + ".jpg";
                category.active = true;
                category.sortOrder = i;
                category.createdAt = LocalDateTime.now().minusDays(random.nextInt(365));
                category.updatedAt = category.createdAt.plusDays(random.nextInt(30));

                entityManager.persist(category);
                categories.add(category);

                if (i % 20 == 0 && i > 0) {
                    entityManager.flush();
                    System.out.println("Generated " + (i + 1) + " categories so far...");
                }
            }

            entityManager.flush();
            System.out.println("Successfully generated " + categories.size() + " categories");
            return categories;
        } catch (Exception e) {
            System.err.println("Error generating categories: " + e.getMessage());
            throw new RuntimeException("Failed to generate categories", e);
        }
    }

    private void generateProductsInBatches(int totalProducts, List<Brand> brands, List<Category> categories) {
        int batchSize = 500; // Reduced batch size for better transaction management
        int batches = (totalProducts + batchSize - 1) / batchSize;

        try {
            System.out.println("Starting product generation in " + batches + " batches of " + batchSize + " products each");

            for (int batch = 0; batch < batches; batch++) {
                int startIndex = batch * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalProducts);
                int currentBatchSize = endIndex - startIndex;

                System.out.println("Generating batch " + (batch + 1) + "/" + batches + " (" + currentBatchSize + " products)");

                try {
                    generateProductBatch(startIndex, currentBatchSize, brands, categories);
                    System.out.println("Completed batch " + (batch + 1) + "/" + batches);
                } catch (Exception e) {
                    System.err.println("Error in batch " + (batch + 1) + ": " + e.getMessage());
                    // Continue with next batch instead of failing completely
                    continue;
                }

                // Small delay between batches to prevent overwhelming the database
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("Product batch generation completed");
        } catch (Exception e) {
            System.err.println("Error during batch generation: " + e.getMessage());
            throw new RuntimeException("Failed to generate products in batches", e);
        }
    }

    @Transactional
    public void generateProductBatch(int startIndex, int batchSize, List<Brand> brands, List<Category> categories) {
        try {
            for (int i = 0; i < batchSize; i++) {
                int productIndex = startIndex + i;
                Product product = generateProduct(productIndex, brands, categories);
                entityManager.persist(product);

                // Flush every 50 products to prevent memory issues
                if (i % 50 == 0 && i > 0) {
                    entityManager.flush();
                }
            }

            // Final flush for the batch
            entityManager.flush();
        } catch (Exception e) {
            System.err.println("Error generating product batch starting at index " + startIndex + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate product batch", e);
        }
    }

    private Product generateProduct(int index, List<Brand> brands, List<Category> categories) {
        Product product = new Product();
        
        // Generate realistic product name
        String adjective = productAdjectives[random.nextInt(productAdjectives.length)];
        String type = productTypes[random.nextInt(productTypes.length)];
        String model = generateModelNumber();
        product.name = adjective + " " + type + " " + model;

        // Generate description
        product.description = generateProductDescription(product.name);

        // Generate SKU
        product.sku = generateSKU(index);

        // Generate slug
        product.slug = product.name.toLowerCase()
            .replace(" ", "-")
            .replace("&", "and")
            .replaceAll("[^a-z0-9-]", "") + "-" + index;
        
        // Generate price (between $10 and $2000)
        BigDecimal price = BigDecimal.valueOf(10 + random.nextDouble() * 1990)
            .setScale(2, RoundingMode.HALF_UP);
        product.price = price;

        // Generate compare at price (10-30% higher)
        BigDecimal compareAtPrice = price.multiply(BigDecimal.valueOf(1.1 + random.nextDouble() * 0.2))
            .setScale(2, RoundingMode.HALF_UP);
        product.compareAtPrice = compareAtPrice;

        // Generate stock quantity
        product.stockQuantity = random.nextInt(1000);
        product.lowStockThreshold = 10 + random.nextInt(40);

        // Generate weight
        product.weight = BigDecimal.valueOf(0.1 + random.nextDouble() * 10)
            .setScale(2, RoundingMode.HALF_UP);
        product.weightUnit = "kg";
        
        // Set flags
        product.active = random.nextDouble() > 0.05; // 95% active
        product.featured = random.nextDouble() > 0.9; // 10% featured
        product.trackInventory = random.nextDouble() > 0.2; // 80% track inventory

        // Generate image URLs
        List<String> imageUrls = generateImageUrls(product.name, 3 + random.nextInt(3));
        product.imageUrls = imageUrls;

        // Generate tags
        List<String> tags = generateTags(product.name, 3 + random.nextInt(5));
        product.tags = tags;

        // Assign random brand and category
        product.brand = brands.get(random.nextInt(brands.size()));
        product.category = categories.get(random.nextInt(categories.size()));

        // Set timestamps
        LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(365));
        product.createdAt = createdAt;
        product.updatedAt = createdAt.plusDays(random.nextInt(30));
        
        return product;
    }

    private String generateUniqueBrandName(int index) {
        String[] prefixes = {"Tech", "Digital", "Smart", "Pro", "Elite", "Prime", "Global", "Mega"};
        String[] suffixes = {"Corp", "Inc", "Ltd", "Systems", "Solutions", "Technologies", "Industries", "Group"};
        
        String prefix = prefixes[index % prefixes.length];
        String suffix = suffixes[(index / prefixes.length) % suffixes.length];
        
        return prefix + suffix + " " + (index + 1);
    }

    private String generateUniqueCategoryName(int index) {
        String[] prefixes = {"Advanced", "Professional", "Premium", "Smart", "Digital", "Modern", "Classic", "Luxury"};
        String[] suffixes = {"Products", "Solutions", "Equipment", "Devices", "Accessories", "Tools", "Systems", "Gear"};
        
        String prefix = prefixes[index % prefixes.length];
        String suffix = suffixes[(index / prefixes.length) % suffixes.length];
        
        return prefix + " " + suffix + " " + (index + 1);
    }

    private String generateModelNumber() {
        return "M" + (1000 + random.nextInt(9000)) + 
               (char)('A' + random.nextInt(26)) + 
               (char)('A' + random.nextInt(26));
    }

    private String generateSKU(int index) {
        return "SKU-" + String.format("%08d", index + 1);
    }

    private String generateProductDescription(String productName) {
        String[] features = {
            "cutting-edge technology", "premium materials", "ergonomic design", "long-lasting durability",
            "exceptional performance", "user-friendly interface", "energy efficiency", "advanced features",
            "sleek aesthetics", "reliable functionality", "innovative engineering", "superior quality"
        };
        
        String feature1 = features[random.nextInt(features.length)];
        String feature2 = features[random.nextInt(features.length)];
        
        return "The " + productName + " combines " + feature1 + " with " + feature2 + 
               " to deliver an outstanding user experience. Perfect for both professional and personal use.";
    }

    private List<String> generateImageUrls(String productName, int count) {
        List<String> urls = new ArrayList<>();
        String baseUrl = "https://example.com/products/" + 
                        productName.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9-]", "");
        
        for (int i = 1; i <= count; i++) {
            urls.add(baseUrl + "-" + i + ".jpg");
        }
        
        return urls;
    }

    private List<String> generateTags(String productName, int count) {
        String[] allTags = {
            "electronics", "technology", "premium", "professional", "portable", "wireless", "smart",
            "durable", "lightweight", "waterproof", "fast", "efficient", "innovative", "modern",
            "bestseller", "trending", "popular", "recommended", "featured", "new", "sale", "discount"
        };
        
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String tag = allTags[random.nextInt(allTags.length)];
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }
        
        return tags;
    }


}
