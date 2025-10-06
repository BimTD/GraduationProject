/**
 * Modern Search JavaScript
 * Handles advanced search functionality with autocomplete, suggestions, and real-time search
 */

class ModernSearch {
    constructor() {
        this.searchInput = null;
        this.suggestionsContainer = null;
        this.searchTimeout = null;
        this.currentRequest = null;
        this.searchHistory = this.loadSearchHistory();
        this.popularSearches = [];
        this.isLoading = false;
        
        this.init();
    }

    init() {
        this.createSearchInterface();
        this.bindEvents();
        this.loadPopularSearches();
    }

    createSearchInterface() {
        // Find existing search form or create new one
        const existingSearch = document.querySelector('.search_bar form');
        if (existingSearch) {
            this.replaceExistingSearch(existingSearch);
        } else {
            this.createNewSearchInterface();
        }
    }

    replaceExistingSearch(existingForm) {
        const searchBar = existingForm.closest('.search_bar');
        if (!searchBar) return;

        // Create modern search interface
        const modernSearchHTML = `
            <div class="modern-search-container">
                <div class="search-input-wrapper">
                    <select class="search-category-select" id="search-category">
                        <option value="">Tất cả danh mục</option>
                    </select>
                    <input type="text" class="search-input" id="modern-search-input" 
                           placeholder="Tìm kiếm sản phẩm..." autocomplete="off">
                    <button type="button" class="search-button" id="search-btn">
                        <i class="ion-ios-search-strong"></i>
                    </button>
                </div>
                <div class="search-suggestions" id="search-suggestions"></div>
            </div>
        `;

        searchBar.innerHTML = modernSearchHTML;
        this.initializeElements();
        this.loadCategories();
    }

    createNewSearchInterface() {
        // Create new search interface if no existing form found
        const searchContainer = document.querySelector('.search_bar') || document.querySelector('.header_middel .col-lg-6');
        if (searchContainer) {
            searchContainer.innerHTML = `
                <div class="modern-search-container">
                    <div class="search-input-wrapper">
                        <select class="search-category-select" id="search-category">
                            <option value="">Tất cả danh mục</option>
                        </select>
                        <input type="text" class="search-input" id="modern-search-input" 
                               placeholder="Tìm kiếm sản phẩm..." autocomplete="off">
                        <button type="button" class="search-button" id="search-btn">
                            <i class="ion-ios-search-strong"></i>
                        </button>
                    </div>
                    <div class="search-suggestions" id="search-suggestions"></div>
                </div>
            `;
            this.initializeElements();
            this.loadCategories();
        }
    }

    initializeElements() {
        this.searchInput = document.getElementById('modern-search-input');
        this.suggestionsContainer = document.getElementById('search-suggestions');
        this.searchButton = document.getElementById('search-btn');
        this.categorySelect = document.getElementById('search-category');
    }

    bindEvents() {
        if (!this.searchInput) return;

        // Input events
        this.searchInput.addEventListener('input', (e) => {
            this.handleSearchInput(e.target.value);
        });

        this.searchInput.addEventListener('focus', () => {
            this.showSuggestions();
        });

        this.searchInput.addEventListener('blur', (e) => {
            // Delay hiding to allow clicking on suggestions
            setTimeout(() => {
                if (!this.suggestionsContainer.contains(document.activeElement)) {
                    this.hideSuggestions();
                }
            }, 200);
        });

        this.searchInput.addEventListener('keydown', (e) => {
            this.handleKeyNavigation(e);
        });

        // Search button click
        if (this.searchButton) {
            this.searchButton.addEventListener('click', () => {
                this.performSearch();
            });
        }

        // Category change
        if (this.categorySelect) {
            this.categorySelect.addEventListener('change', () => {
                this.handleSearchInput(this.searchInput.value);
            });
        }

        // Click outside to close suggestions
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.modern-search-container')) {
                this.hideSuggestions();
            }
        });
    }

    handleSearchInput(query) {
        // Clear previous timeout
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        // Cancel previous request
        if (this.currentRequest) {
            this.currentRequest.abort();
        }

        if (!query || query.trim().length < 2) {
            this.showDefaultSuggestions();
            return;
        }

        // Debounce search
        this.searchTimeout = setTimeout(() => {
            this.fetchSuggestions(query.trim());
        }, 300);
    }

    async fetchSuggestions(query) {
        if (this.isLoading) return;

        this.isLoading = true;
        this.showLoading();

        try {
            const categoryId = this.categorySelect ? this.categorySelect.value : '';
            const url = `/api/search/suggestions?q=${encodeURIComponent(query)}&limit=8`;
            
            this.currentRequest = fetch(url);
            const response = await this.currentRequest;
            
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            this.displaySuggestions(data);
            
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('Error fetching suggestions:', error);
                this.showError('Lỗi tải gợi ý tìm kiếm');
            }
        } finally {
            this.isLoading = false;
            this.currentRequest = null;
        }
    }

    displaySuggestions(data) {
        if (!this.suggestionsContainer) return;

        console.log('displaySuggestions called with data:', data);

        let html = '';

        if (data.success && data.suggestions && data.suggestions.length > 0) {
            // Show product suggestions
            html += '<div class="suggestions-list">';
            data.suggestions.forEach((product, index) => {
                console.log(`Product ${index}:`, product);
                
                const imageUrl = product.imageUrl || '/fe/img/product/product15.jpg';
                const price = product.giaBan || '0 VNĐ';
                const category = product.categoryName || '';
                const productId = product.id || product.productId || product.sanPhamId;
                
                console.log(`Product ${index} ID:`, productId);

                html += `
                    <div class="suggestion-item" data-product-id="${productId}">
                        <img src="${imageUrl}" alt="${product.ten}" class="suggestion-image" 
                             onerror="this.src='/fe/img/product/product15.jpg'">
                        <div class="suggestion-content">
                            <div class="suggestion-title">${product.ten}</div>
                            <div class="suggestion-price">${price}</div>
                            <div class="suggestion-category">${category}</div>
                        </div>
                    </div>
                `;
            });
            html += '</div>';

            // Add click handlers for suggestions
            this.suggestionsContainer.innerHTML = html;
            this.bindSuggestionEvents();
        } else {
            // Show popular searches and history
            this.showDefaultSuggestions();
        }

        this.showSuggestions();
    }

    showDefaultSuggestions() {
        if (!this.suggestionsContainer) return;

        let html = '';

        // Show bestselling products first
        html += `
            <div class="bestselling-products">
                <h4>Sản phẩm bán chạy nhất</h4>
                <div class="bestselling-loading">
                    <div class="spinner"></div>
                    <p>Đang tải sản phẩm bán chạy...</p>
                </div>
            </div>
        `;

        // Show search history
        if (this.searchHistory.length > 0) {
            html += `
                <div class="search-history">
                    <h4>Lịch sử tìm kiếm</h4>
                    ${this.searchHistory.slice(0, 5).map(term => `
                        <div class="history-item" data-query="${term}">
                            <span>${term}</span>
                            <span class="remove-history" data-query="${term}">×</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        // Show popular searches
        if (this.popularSearches.length > 0) {
            html += `
                <div class="popular-searches">
                    <h4>Tìm kiếm phổ biến</h4>
                    <div class="popular-tags">
                        ${this.popularSearches.map(term => `
                            <span class="popular-tag" data-query="${term}">${term}</span>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        this.suggestionsContainer.innerHTML = html;
        this.bindDefaultSuggestionEvents();
        this.showSuggestions();
        
        // Load bestselling products
        this.loadBestsellingProducts();
    }

    bindSuggestionEvents() {
        const suggestionItems = this.suggestionsContainer.querySelectorAll('.suggestion-item');
        console.log('Binding events to', suggestionItems.length, 'suggestion items');
        
        suggestionItems.forEach((item, index) => {
            const productId = item.dataset.productId;
            console.log(`Item ${index}: productId = ${productId}`);
            
            // Remove existing listeners to avoid duplicates
            item.removeEventListener('click', this.handleSuggestionClick);
            
            // Add new listener
            item.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                console.log('Suggestion clicked, productId:', productId);
                
                if (productId && productId !== 'undefined' && productId !== 'null') {
                    this.navigateToProduct(productId);
                } else {
                    console.error('Invalid product ID:', productId);
                    alert('Không thể tìm thấy sản phẩm này');
                }
            });
            
            // Thêm cursor pointer để user biết có thể click
            item.style.cursor = 'pointer';
        });
    }

    bindDefaultSuggestionEvents() {
        // History items
        const historyItems = this.suggestionsContainer.querySelectorAll('.history-item');
        historyItems.forEach(item => {
            item.addEventListener('click', (e) => {
                if (e.target.classList.contains('remove-history')) {
                    this.removeFromHistory(e.target.dataset.query);
                } else {
                    const query = item.dataset.query;
                    this.searchInput.value = query;
                    this.performSearch();
                }
            });
        });

        // Popular tags
        const popularTags = this.suggestionsContainer.querySelectorAll('.popular-tag');
        popularTags.forEach(tag => {
            tag.addEventListener('click', () => {
                const query = tag.dataset.query;
                this.searchInput.value = query;
                this.performSearch();
            });
        });
    }

    handleKeyNavigation(e) {
        const suggestions = this.suggestionsContainer.querySelectorAll('.suggestion-item, .history-item, .popular-tag, .bestselling-item');
        const current = document.activeElement;
        let currentIndex = -1;

        suggestions.forEach((item, index) => {
            if (item === current) {
                currentIndex = index;
            }
        });

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                if (currentIndex < suggestions.length - 1) {
                    suggestions[currentIndex + 1].focus();
                }
                break;
            case 'ArrowUp':
                e.preventDefault();
                if (currentIndex > 0) {
                    suggestions[currentIndex - 1].focus();
                } else {
                    this.searchInput.focus();
                }
                break;
            case 'Enter':
                e.preventDefault();
                if (current && current !== this.searchInput) {
                    current.click();
                } else {
                    this.performSearch();
                }
                break;
            case 'Escape':
                this.hideSuggestions();
                this.searchInput.blur();
                break;
        }
    }

    performSearch() {
        const query = this.searchInput.value.trim();
        if (!query) return;

        // Save to search history
        this.addToHistory(query);

        // Navigate to shop page with search parameters
        const categoryId = this.categorySelect ? this.categorySelect.value : '';
        let url = `/shop?search=${encodeURIComponent(query)}`;
        
        if (categoryId) {
            url += `&categoryId=${categoryId}`;
        }

        window.location.href = url;
    }

    navigateToProduct(productId) {
        console.log('navigateToProduct called with productId:', productId);
        
        if (productId && productId !== 'undefined' && productId !== 'null') {
            // Ẩn suggestions trước khi chuyển trang
            this.hideSuggestions();
            
            // Validate product ID is a number
            const numericId = parseInt(productId);
            if (isNaN(numericId) || numericId <= 0) {
                console.error('Invalid numeric product ID:', productId);
                alert('ID sản phẩm không hợp lệ');
                return;
            }
            
            // Chuyển đến trang chi tiết sản phẩm
            const url = `/product-details/${numericId}`;
            console.log('Navigating to:', url);
            window.location.href = url;
        } else {
            console.error('Invalid product ID:', productId);
            alert('Không thể tìm thấy sản phẩm này');
        }
    }

    showSuggestions() {
        if (this.suggestionsContainer) {
            this.suggestionsContainer.classList.add('show');
        }
    }

    hideSuggestions() {
        if (this.suggestionsContainer) {
            this.suggestionsContainer.classList.remove('show');
        }
    }

    showLoading() {
        if (this.suggestionsContainer) {
            this.suggestionsContainer.innerHTML = `
                <div class="search-loading">
                    <div class="spinner"></div>
                </div>
            `;
            this.showSuggestions();
        }
    }

    showError(message) {
        if (this.suggestionsContainer) {
            this.suggestionsContainer.innerHTML = `
                <div class="no-results">
                    <i class="fa fa-exclamation-triangle"></i>
                    <p>${message}</p>
                </div>
            `;
            this.showSuggestions();
        }
    }

    async loadCategories() {
        try {
            const response = await fetch('/api/categories');
            if (response.ok) {
                const data = await response.json();
                if (data.success && data.categories) {
                    const categorySelect = document.getElementById('search-category');
                    if (categorySelect) {
                        data.categories.forEach(category => {
                            const option = document.createElement('option');
                            option.value = category.id;
                            option.textContent = category.ten;
                            categorySelect.appendChild(option);
                        });
                    }
                }
            }
        } catch (error) {
            console.error('Error loading categories:', error);
        }
    }

    async loadPopularSearches() {
        try {
            const response = await fetch('/api/search/popular?limit=10');
            if (response.ok) {
                const data = await response.json();
                if (data.success && data.popularSearches) {
                    this.popularSearches = data.popularSearches;
                }
            }
        } catch (error) {
            console.error('Error loading popular searches:', error);
        }
    }

    async loadBestsellingProducts() {
        try {
            const response = await fetch('/api/search/bestsellers?limit=6');
            if (response.ok) {
                const data = await response.json();
                if (data.success && data.suggestions && data.suggestions.length > 0) {
                    this.displayBestsellingProducts(data.suggestions);
                } else {
                    this.hideBestsellingLoading();
                }
            } else {
                this.hideBestsellingLoading();
            }
        } catch (error) {
            console.error('Error loading bestselling products:', error);
            this.hideBestsellingLoading();
        }
    }

    displayBestsellingProducts(products) {
        const bestsellingContainer = this.suggestionsContainer.querySelector('.bestselling-products');
        if (!bestsellingContainer) return;

        let html = '<h4>Sản phẩm bán chạy nhất</h4>';
        html += '<div class="bestselling-list">';
        
        products.forEach((product, index) => {
            const imageUrl = product.imageUrl || '/fe/img/product/product15.jpg';
            const price = product.giaBan || '0 VNĐ';
            const productId = product.id || product.productId || product.sanPhamId;
            
            html += `
                <div class="bestselling-item" data-product-id="${productId}">
                    <img src="${imageUrl}" alt="${product.ten}" class="bestselling-image" 
                         onerror="this.src='/fe/img/product/product15.jpg'">
                    <div class="bestselling-content">
                        <div class="bestselling-title">${product.ten}</div>
                        <div class="bestselling-price">${price}</div>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
        bestsellingContainer.innerHTML = html;
        
        // Bind click events for bestselling products
        this.bindBestsellingEvents();
    }

    hideBestsellingLoading() {
        const bestsellingContainer = this.suggestionsContainer.querySelector('.bestselling-products');
        if (bestsellingContainer) {
            const loadingDiv = bestsellingContainer.querySelector('.bestselling-loading');
            if (loadingDiv) {
                loadingDiv.style.display = 'none';
            }
        }
    }

    bindBestsellingEvents() {
        const bestsellingItems = this.suggestionsContainer.querySelectorAll('.bestselling-item');
        
        bestsellingItems.forEach((item) => {
            const productId = item.dataset.productId;
            
            item.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                
                if (productId && productId !== 'undefined' && productId !== 'null') {
                    this.navigateToProduct(productId);
                } else {
                    console.error('Invalid product ID:', productId);
                    alert('Không thể tìm thấy sản phẩm này');
                }
            });
            
            // Add cursor pointer
            item.style.cursor = 'pointer';
        });
    }

    addToHistory(query) {
        if (!query || query.trim().length < 2) return;

        const trimmedQuery = query.trim();
        
        // Remove if already exists
        this.searchHistory = this.searchHistory.filter(item => item !== trimmedQuery);
        
        // Add to beginning
        this.searchHistory.unshift(trimmedQuery);
        
        // Keep only last 10
        this.searchHistory = this.searchHistory.slice(0, 10);
        
        this.saveSearchHistory();
    }

    removeFromHistory(query) {
        this.searchHistory = this.searchHistory.filter(item => item !== query);
        this.saveSearchHistory();
        this.showDefaultSuggestions();
    }

    loadSearchHistory() {
        try {
            const history = localStorage.getItem('searchHistory');
            return history ? JSON.parse(history) : [];
        } catch (error) {
            console.error('Error loading search history:', error);
            return [];
        }
    }

    saveSearchHistory() {
        try {
            localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory));
        } catch (error) {
            console.error('Error saving search history:', error);
        }
    }

    formatPrice(price) {
        if (!price) return '0 VNĐ';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on a page that needs search functionality
    if (document.querySelector('.search_bar') || document.querySelector('.header_middel')) {
        new ModernSearch();
    }
});

// Export for potential use in other scripts
window.ModernSearch = ModernSearch;
