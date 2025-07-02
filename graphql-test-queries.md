# GraphQL Field Selection Optimization - Complete Testing Guide

## 🚀 Data Generation for Performance Testing

### Generate 50,000 Products for Large-Scale Testing

**Web UI**: http://localhost:8080/quinoa
- Click "Generate 50,000 Products" button
- Monitor progress in server logs
- Takes 5-10 minutes to complete

**API Endpoints**:
```bash
# Generate 50K products (100 brands, 50 categories, 50,000 products)
curl -X POST http://localhost:8080/api/data-generation/generate-50k

# Custom generation
curl -X POST "http://localhost:8080/api/data-generation/generate-custom?brands=20&categories=15&products=5000"

# Health check
curl -X GET http://localhost:8080/api/data-generation/health
```

## 📊 GraphQL Performance Testing

Test these queries in the GraphQL UI at http://localhost:8080/q/graphql-ui

## 1. Basic Fields Only (Optimized)
```graphql
query TestBasicProducts {
  productsBasic(pageIndex: 0, pageSize: 5) {
    id
    name
    sku
    price
    stockQuantity
  }
}
```

## 2. With Brand and Category (Selective Loading)
```graphql
query TestProductsWithRelations {
  productsWithBrandAndCategory(pageIndex: 0, pageSize: 5) {
    id
    name
    sku
    price
    stockQuantity
    brand {
      id
      name
      logoUrl
    }
    category {
      id
      name
      slug
    }
  }
}
```

## 3. All Fields (Complete Entity)
```graphql
query TestAllFields {
  productsWithPagination(pageIndex: 0, pageSize: 5) {
    id
    name
    description
    sku
    slug
    price
    compareAtPrice
    stockQuantity
    lowStockThreshold
    weight
    weightUnit
    active
    featured
    trackInventory
    imageUrls
    tags
    createdAt
    updatedAt
    brand {
      id
      name
      description
      logoUrl
      websiteUrl
      active
      createdAt
      updatedAt
    }
    category {
      id
      name
      description
      slug
      imageUrl
      active
      sortOrder
      createdAt
      updatedAt
    }
  }
}
```

## 4. Search Basic
```graphql
query TestSearchBasic {
  searchProductsBasic(namePattern: "iPhone", pageIndex: 0, pageSize: 5) {
    id
    name
    sku
    price
    stockQuantity
  }
}
```

## 5. Search with Relations
```graphql
query TestSearchWithRelations {
  searchProductsWithBrandAndCategory(namePattern: "iPhone", pageIndex: 0, pageSize: 5) {
    id
    name
    sku
    price
    stockQuantity
    brand {
      id
      name
      logoUrl
    }
    category {
      id
      name
      slug
    }
  }
}
```

## 6. Search All Fields
```graphql
query TestSearchAllFields {
  searchProductsWithPagination(namePattern: "iPhone", pageIndex: 0, pageSize: 5) {
    id
    name
    description
    sku
    slug
    price
    compareAtPrice
    stockQuantity
    lowStockThreshold
    weight
    weightUnit
    active
    featured
    trackInventory
    imageUrls
    tags
    createdAt
    updatedAt
    brand {
      id
      name
      description
      logoUrl
      websiteUrl
      active
      createdAt
      updatedAt
    }
    category {
      id
      name
      description
      slug
      imageUrl
      active
      sortOrder
      createdAt
      updatedAt
    }
  }
}
```

## 7. Featured Products Basic
```graphql
query TestFeaturedBasic {
  featuredProductsBasic(pageIndex: 0, pageSize: 5) {
    id
    name
    sku
    price
    stockQuantity
  }
}
```

## Performance Comparison

### Expected SQL for Basic Query:
```sql
SELECT p.id, p.name, p.sku, p.price, p.stock_quantity 
FROM products p 
WHERE p.active = true 
ORDER BY p.name
```

### Expected SQL for With Relations Query:
```sql
SELECT p.id, p.name, p.sku, p.price, p.stock_quantity,
       b.id, b.name, b.logo_url,
       c.id, c.name, c.slug
FROM products p 
LEFT JOIN brands b ON p.brand_id = b.id
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.active = true 
ORDER BY p.name
```

### Expected SQL for All Fields Query:
```sql
SELECT p.*, b.*, c.*
FROM products p 
LEFT JOIN brands b ON p.brand_id = b.id
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.active = true 
ORDER BY p.name
```

## Performance Benefits

1. **Basic Query**: ~75% reduction in data transfer
2. **With Relations**: ~50% reduction in data transfer
3. **All Fields**: Full entity fetch (baseline)

The optimized queries should show significantly better performance in the web UI performance metrics.
