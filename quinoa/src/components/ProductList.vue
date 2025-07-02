<template>
  <div class="product-list">
    <h2>Product Management</h2>
    
    <!-- Query Type Selector -->
    <div class="query-selector">
      <h3>Select Query Type to See Performance Difference:</h3>
      <div class="radio-group">
        <label>
          <input type="radio" v-model="queryType" value="basic" />
          Basic Fields Only (Optimized)
        </label>
        <label>
          <input type="radio" v-model="queryType" value="withRelations" />
          With Brand & Category (Full Query)
        </label>
        <label>
          <input type="radio" v-model="queryType" value="allFields" />
          All Fields (Heaviest Query)
        </label>
      </div>
    </div>

    <!-- Performance Metrics -->
    <div class="performance-metrics" v-if="lastQueryTime">
      <h4>Last Query Performance:</h4>
      <p><strong>Query Time:</strong> {{ lastQueryTime }}ms</p>
      <p><strong>Fields Requested:</strong> {{ requestedFields.join(', ') }}</p>
      <p><strong>Records Returned:</strong> {{ products.length }}</p>
    </div>

    <!-- Search -->
    <div class="search-section">
      <input 
        v-model="searchTerm" 
        placeholder="Search products..." 
        @input="debouncedSearch"
        class="search-input"
      />
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading products...</div>

    <!-- Error State -->
    <div v-if="error" class="error">{{ error }}</div>

    <!-- Products Grid -->
    <div v-if="!loading && !error" class="products-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <h3>{{ product.name }}</h3>
        <p class="sku" v-if="product.sku">SKU: {{ product.sku }}</p>
        <p class="price">${{ product.price }}</p>
        
        <!-- Show brand only if requested -->
        <p v-if="product.brand" class="brand">
          Brand: {{ product.brand.name }}
        </p>
        
        <!-- Show category only if requested -->
        <p v-if="product.category" class="category">
          Category: {{ product.category.name }}
        </p>
        
        <!-- Show description only if requested -->
        <p v-if="product.description" class="description">
          {{ product.description.substring(0, 100) }}...
        </p>
        
        <!-- Show stock info only if requested -->
        <div v-if="product.stockQuantity !== undefined" class="stock-info">
          <span :class="{ 'low-stock': product.stockQuantity <= 10 }">
            Stock: {{ product.stockQuantity }}
          </span>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="!loading && products.length > 0">
      <button @click="previousPage" :disabled="currentPage === 0">Previous</button>
      <span>Page {{ currentPage + 1 }}</span>
      <button @click="nextPage" :disabled="products.length < pageSize">Next</button>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'

export default {
  name: 'ProductList',
  setup() {
    const products = ref([])
    const loading = ref(false)
    const error = ref(null)
    const searchTerm = ref('')
    const queryType = ref('basic')
    const currentPage = ref(0)
    const pageSize = ref(20)
    const lastQueryTime = ref(null)
    const requestedFields = ref([])

    // GraphQL queries for different scenarios
    const queries = {
      basic: `
        query GetProducts($pageIndex: Int, $pageSize: Int) {
          productsWithPagination(pageIndex: $pageIndex, pageSize: $pageSize) {
            id
            name
            sku
            price
            stockQuantity
          }
        }
      `,
      withRelations: `
        query GetProducts($pageIndex: Int, $pageSize: Int) {
          productsWithPagination(pageIndex: $pageIndex, pageSize: $pageSize) {
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
      `,
      allFields: `
        query GetProducts($pageIndex: Int, $pageSize: Int) {
          productsWithPagination(pageIndex: $pageIndex, pageSize: $pageSize) {
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
      `
    }

    const searchQueries = {
      basic: `
        query SearchProducts($namePattern: String!, $pageIndex: Int, $pageSize: Int) {
          searchProductsWithPagination(namePattern: $namePattern, pageIndex: $pageIndex, pageSize: $pageSize) {
            id
            name
            sku
            price
            stockQuantity
          }
        }
      `,
      withRelations: `
        query SearchProducts($namePattern: String!, $pageIndex: Int, $pageSize: Int) {
          searchProductsWithPagination(namePattern: $namePattern, pageIndex: $pageIndex, pageSize: $pageSize) {
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
      `,
      allFields: `
        query SearchProducts($namePattern: String!, $pageIndex: Int, $pageSize: Int) {
          searchProductsWithPagination(namePattern: $namePattern, pageIndex: $pageIndex, pageSize: $pageSize) {
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
      `
    }

    const executeGraphQLQuery = async (query, variables = {}) => {
      const startTime = performance.now()
      
      try {
        const response = await fetch('/graphql', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            query,
            variables
          })
        })

        const result = await response.json()
        const endTime = performance.now()
        
        lastQueryTime.value = Math.round(endTime - startTime)
        
        if (result.errors) {
          throw new Error(result.errors[0].message)
        }

        return result.data
      } catch (err) {
        const endTime = performance.now()
        lastQueryTime.value = Math.round(endTime - startTime)
        throw err
      }
    }

    const loadProducts = async () => {
      loading.value = true
      error.value = null

      try {
        const query = searchTerm.value 
          ? searchQueries[queryType.value]
          : queries[queryType.value]
        
        const variables = {
          pageIndex: currentPage.value,
          pageSize: pageSize.value
        }

        if (searchTerm.value) {
          variables.namePattern = searchTerm.value
        }

        // Track requested fields for performance metrics
        requestedFields.value = extractFieldsFromQuery(query)

        const data = await executeGraphQLQuery(query, variables)
        
        products.value = searchTerm.value 
          ? data.searchProductsWithPagination
          : data.productsWithPagination

      } catch (err) {
        error.value = err.message
        console.error('Error loading products:', err)
      } finally {
        loading.value = false
      }
    }

    const extractFieldsFromQuery = (query) => {
      // Simple field extraction for demo purposes
      const fields = []
      if (query.includes('name')) fields.push('name')
      if (query.includes('sku')) fields.push('sku')
      if (query.includes('price')) fields.push('price')
      if (query.includes('description')) fields.push('description')
      if (query.includes('brand {')) fields.push('brand')
      if (query.includes('category {')) fields.push('category')
      if (query.includes('imageUrls')) fields.push('imageUrls')
      if (query.includes('tags')) fields.push('tags')
      return fields
    }

    // Debounced search
    let searchTimeout
    const debouncedSearch = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        currentPage.value = 0
        loadProducts()
      }, 300)
    }

    const nextPage = () => {
      currentPage.value++
      loadProducts()
    }

    const previousPage = () => {
      if (currentPage.value > 0) {
        currentPage.value--
        loadProducts()
      }
    }

    // Watch for query type changes
    watch(queryType, () => {
      currentPage.value = 0
      loadProducts()
    })

    // Load products on mount
    onMounted(() => {
      loadProducts()
    })

    return {
      products,
      loading,
      error,
      searchTerm,
      queryType,
      currentPage,
      pageSize,
      lastQueryTime,
      requestedFields,
      loadProducts,
      debouncedSearch,
      nextPage,
      previousPage
    }
  }
}
</script>

<style scoped>
.product-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.query-selector {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.performance-metrics {
  background: #e8f5e8;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #4caf50;
}

.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  font-size: 18px;
}

.error {
  color: #d32f2f;
  background: #ffebee;
  border-radius: 4px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-card h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.sku {
  color: #666;
  font-size: 14px;
  margin: 4px 0;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #2e7d32;
  margin: 8px 0;
}

.brand, .category {
  font-size: 14px;
  color: #555;
  margin: 4px 0;
}

.description {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
}

.stock-info {
  margin-top: 8px;
}

.low-stock {
  color: #d32f2f;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:not(:disabled):hover {
  background: #f5f5f5;
}
</style>
