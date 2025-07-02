import './style.css'

// Application state
let currentPage = 0;
let pageSize = 20;
let currentQueryType = 'basic';
let searchTerm = '';
let lastQueryTime = null;
let requestedFields = [];

// GraphQL queries for different scenarios
const queries = {
  basic: `
    query GetProducts($pageIndex: Int, $pageSize: Int) {
      productsBasic(pageIndex: $pageIndex, pageSize: $pageSize) {
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
      productsWithBrandAndCategory(pageIndex: $pageIndex, pageSize: $pageSize) {
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
};

const searchQueries = {
  basic: `
    query SearchProducts($namePattern: String!, $pageIndex: Int, $pageSize: Int) {
      searchProductsBasic(namePattern: $namePattern, pageIndex: $pageIndex, pageSize: $pageSize) {
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
      searchProductsWithBrandAndCategory(namePattern: $namePattern, pageIndex: $pageIndex, pageSize: $pageSize) {
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
};

// Execute GraphQL query with performance tracking
async function executeGraphQLQuery(query, variables = {}) {
  const startTime = performance.now();

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
    });

    const result = await response.json();
    const endTime = performance.now();

    lastQueryTime = Math.round(endTime - startTime);

    if (result.errors) {
      throw new Error(result.errors[0].message);
    }

    return result.data;
  } catch (err) {
    const endTime = performance.now();
    lastQueryTime = Math.round(endTime - startTime);
    throw err;
  }
}

// Extract fields from query for performance metrics
function extractFieldsFromQuery(query) {
  const fields = [];

  // Basic fields
  if (query.includes('id')) fields.push('id');
  if (query.includes('name')) fields.push('name');
  if (query.includes('sku')) fields.push('sku');
  if (query.includes('price')) fields.push('price');
  if (query.includes('stockQuantity')) fields.push('stockQuantity');

  // Extended fields
  if (query.includes('description')) fields.push('description');
  if (query.includes('slug')) fields.push('slug');
  if (query.includes('compareAtPrice')) fields.push('compareAtPrice');
  if (query.includes('weight')) fields.push('weight');
  if (query.includes('active')) fields.push('active');
  if (query.includes('featured')) fields.push('featured');
  if (query.includes('imageUrls')) fields.push('imageUrls');
  if (query.includes('tags')) fields.push('tags');
  if (query.includes('createdAt')) fields.push('createdAt');
  if (query.includes('updatedAt')) fields.push('updatedAt');

  // Relationships
  if (query.includes('brand {')) fields.push('brand');
  if (query.includes('category {')) fields.push('category');

  return fields;
}

// Load products based on current settings
async function loadProducts() {
  const loadingState = document.getElementById('loadingState');
  const errorState = document.getElementById('errorState');
  const productsGrid = document.getElementById('productsGrid');
  const performanceMetrics = document.getElementById('performanceMetrics');

  // Show loading state
  loadingState.style.display = 'flex';
  errorState.style.display = 'none';
  productsGrid.innerHTML = '';

  try {
    const query = searchTerm
      ? searchQueries[currentQueryType]
      : queries[currentQueryType];

    const variables = {
      pageIndex: currentPage,
      pageSize: pageSize
    };

    if (searchTerm) {
      variables.namePattern = searchTerm;
    }

    // Track requested fields for performance metrics
    requestedFields = extractFieldsFromQuery(query);

    const data = await executeGraphQLQuery(query, variables);

    console.log('GraphQL Response:', data);
    console.log('Current Query Type:', currentQueryType);
    console.log('Search Term:', searchTerm);

    let products;
    if (searchTerm) {
      if (currentQueryType === 'basic') {
        products = data.searchProductsBasic;
      } else if (currentQueryType === 'withRelations') {
        products = data.searchProductsWithBrandAndCategory;
      } else {
        products = data.searchProductsWithPagination;
      }
    } else {
      if (currentQueryType === 'basic') {
        products = data.productsBasic;
      } else if (currentQueryType === 'withRelations') {
        products = data.productsWithBrandAndCategory;
      } else {
        products = data.productsWithPagination;
      }
    }

    console.log('Selected Products:', products);

    // Update performance metrics
    updatePerformanceMetrics(products ? products.length : 0);

    // Render products
    renderProducts(products || []);

    // Update pagination
    updatePagination(products ? products.length : 0);

  } catch (err) {
    errorState.textContent = err.message;
    errorState.style.display = 'block';
    console.error('Error loading products:', err);
  } finally {
    loadingState.style.display = 'none';
  }
}

// Update performance metrics display
function updatePerformanceMetrics(productsCount) {
  const performanceMetrics = document.getElementById('performanceMetrics');
  const queryTime = document.getElementById('queryTime');
  const fieldsRequested = document.getElementById('fieldsRequested');
  const recordsReturned = document.getElementById('recordsReturned');

  queryTime.textContent = `${lastQueryTime}ms`;
  fieldsRequested.textContent = requestedFields.join(', ');
  recordsReturned.textContent = productsCount || 0;

  performanceMetrics.style.display = 'block';
}

// Render products in the grid
function renderProducts(products) {
  const productsGrid = document.getElementById('productsGrid');

  if (!products || !Array.isArray(products) || products.length === 0) {
    productsGrid.innerHTML = '<div class="no-products">No products found</div>';
    return;
  }

  try {
    productsGrid.innerHTML = products.map(product => {
      if (!product) return '';

      return `
        <div class="product-card">
          <h3>${product.name || 'Unknown Product'}</h3>
          ${product.sku ? `<div class="product-sku">SKU: ${product.sku}</div>` : ''}
          <div class="product-price">$${product.price || '0.00'}</div>
          ${product.brand ? `<div class="product-brand">Brand: ${product.brand.name || 'Unknown'}</div>` : ''}
          ${product.category ? `<div class="product-category">Category: ${product.category.name || 'Unknown'}</div>` : ''}
          ${product.description ? `<div class="product-description">${product.description.substring(0, 100)}...</div>` : ''}
          ${product.stockQuantity !== undefined ? `
            <div class="product-stock ${product.stockQuantity <= 10 ? 'low-stock' : ''}">
              Stock: ${product.stockQuantity}
            </div>
          ` : ''}
        </div>
      `;
    }).filter(html => html).join('');
  } catch (error) {
    console.error('Error rendering products:', error);
    productsGrid.innerHTML = '<div class="error-message">Error displaying products. Please try again.</div>';
  }
}

// Update pagination controls
function updatePagination(productsCount) {
  const pagination = document.getElementById('pagination');
  const prevBtn = document.getElementById('prevBtn');
  const nextBtn = document.getElementById('nextBtn');
  const pageInfo = document.getElementById('pageInfo');

  prevBtn.disabled = currentPage === 0;
  nextBtn.disabled = productsCount < pageSize;
  pageInfo.textContent = `Page ${currentPage + 1}`;

  pagination.style.display = 'flex';
}

// Data generation functions
async function generate50KProducts() {
  const generate50kBtn = document.getElementById('generate50kBtn');
  const generateCustomBtn = document.getElementById('generateCustomBtn');
  const generationStatus = document.getElementById('generationStatus');

  // Disable buttons
  generate50kBtn.disabled = true;
  generateCustomBtn.disabled = true;

  // Show progress status
  generationStatus.className = 'generation-status progress';
  generationStatus.textContent = 'Generating 50,000 products... This may take several minutes.';
  generationStatus.style.display = 'block';

  try {
    const response = await fetch('/api/data-generation/generate-50k', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    const result = await response.json();

    if (response.ok) {
      generationStatus.className = 'generation-status success';
      generationStatus.textContent = 'Data generation started! Check server logs for progress. This will take several minutes.';

      // Auto-refresh products after a delay
      setTimeout(() => {
        loadProducts();
      }, 5000);
    } else {
      throw new Error(result.message || 'Failed to start data generation');
    }
  } catch (error) {
    generationStatus.className = 'generation-status error';
    generationStatus.textContent = 'Error: ' + error.message;
    console.error('Data generation error:', error);
  } finally {
    // Re-enable buttons after a delay
    setTimeout(() => {
      generate50kBtn.disabled = false;
      generateCustomBtn.disabled = false;
    }, 10000);
  }
}

async function generateCustomData() {
  const brands = prompt('Number of brands to generate:', '20');
  const categories = prompt('Number of categories to generate:', '15');
  const products = prompt('Number of products to generate:', '5000');

  if (!brands || !categories || !products) {
    return; // User cancelled
  }

  const generate50kBtn = document.getElementById('generate50kBtn');
  const generateCustomBtn = document.getElementById('generateCustomBtn');
  const generationStatus = document.getElementById('generationStatus');

  // Disable buttons
  generate50kBtn.disabled = true;
  generateCustomBtn.disabled = true;

  // Show progress status
  generationStatus.className = 'generation-status progress';
  generationStatus.textContent = `Generating ${brands} brands, ${categories} categories, and ${products} products...`;
  generationStatus.style.display = 'block';

  try {
    const response = await fetch(`/api/data-generation/generate-custom?brands=${brands}&categories=${categories}&products=${products}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    const result = await response.json();

    if (response.ok) {
      generationStatus.className = 'generation-status success';
      generationStatus.textContent = `Custom data generation started! Generating ${brands} brands, ${categories} categories, and ${products} products.`;

      // Auto-refresh products after a delay
      setTimeout(() => {
        loadProducts();
      }, 3000);
    } else {
      throw new Error(result.message || 'Failed to start custom data generation');
    }
  } catch (error) {
    generationStatus.className = 'generation-status error';
    generationStatus.textContent = 'Error: ' + error.message;
    console.error('Custom data generation error:', error);
  } finally {
    // Re-enable buttons after a delay
    setTimeout(() => {
      generate50kBtn.disabled = false;
      generateCustomBtn.disabled = false;
    }, 5000);
  }
}

// Event handlers
function setupEventHandlers() {
  // Data generation buttons
  document.getElementById('generate50kBtn').addEventListener('click', generate50KProducts);
  document.getElementById('generateCustomBtn').addEventListener('click', generateCustomData);

  // Query type radio buttons
  document.querySelectorAll('input[name="queryType"]').forEach(radio => {
    radio.addEventListener('change', (e) => {
      currentQueryType = e.target.value;
      currentPage = 0;
      loadProducts();
    });
  });

  // Search input with debouncing
  const searchInput = document.getElementById('searchInput');
  let searchTimeout;
  searchInput.addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      searchTerm = e.target.value.trim();
      currentPage = 0;
      loadProducts();
    }, 300);
  });

  // Load products button
  document.getElementById('loadProductsBtn').addEventListener('click', () => {
    loadProducts();
  });

  // Pagination buttons
  document.getElementById('prevBtn').addEventListener('click', () => {
    if (currentPage > 0) {
      currentPage--;
      loadProducts();
    }
  });

  document.getElementById('nextBtn').addEventListener('click', () => {
    currentPage++;
    loadProducts();
  });
}

// Initialize the application
function init() {
  setupEventHandlers();
  loadProducts(); // Load initial data
}

// Start the application when DOM is ready
document.addEventListener('DOMContentLoaded', init);
