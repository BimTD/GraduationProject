(function(){
  const sizeEl = document.getElementById('sizeSelect');
  const colorEl = document.getElementById('colorSelect');
  const stockEl = document.getElementById('stockDisplay');
  const qtyEl = document.getElementById('quantityInput');
  const addToCartBtn = document.getElementById('addToCartBtn');

  let productData = null;
  let allSizes = [];
  let allColors = [];

  function niceSelectUpdate(){
    if (window.jQuery && jQuery.fn.niceSelect) {
      jQuery('#sizeSelect').niceSelect('update');
      jQuery('#colorSelect').niceSelect('update');
    }
  }

  function populateSizes(list, preserveValue){
    const keep = preserveValue ? String(preserveValue) : '';
    sizeEl.innerHTML = '<option value="">Chọn kích thước</option>';
    let canKeep = false;
    (list||[]).forEach(s => {
      const opt = document.createElement('option');
      opt.value = s.id; opt.textContent = s.name;
      if (keep && String(s.id) === keep) { canKeep = true; }
      sizeEl.appendChild(opt);
    });
    sizeEl.value = canKeep ? keep : '';
    niceSelectUpdate();
  }

  function populateColors(list, preserveValue){
    const keep = preserveValue ? String(preserveValue) : '';
    colorEl.innerHTML = '<option value="">Chọn màu sắc</option>';
    let canKeep = false;
    (list||[]).forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.id; opt.textContent = c.name;
      if (keep && String(c.id) === keep) { canKeep = true; }
      colorEl.appendChild(opt);
    });
    colorEl.value = canKeep ? keep : '';
    niceSelectUpdate();
  }

  function updateStock(){
    if(!productData){ 
      stockEl.textContent = '0';
      stockEl.style.color = '#e74c3c';
      qtyEl.max = 0;
      addToCartBtn.disabled = true;
      addToCartBtn.textContent = 'Chọn size và màu sắc';
      return; 
    }
    
    // Kiểm tra xem sản phẩm có variant không
    if (!productData.variants || productData.variants.length === 0) {
      stockEl.textContent = 'Không có biến thể';
      stockEl.style.color = '#e74c3c';
      qtyEl.max = 0;
      addToCartBtn.disabled = true;
      addToCartBtn.textContent = 'Không có biến thể';
      return;
    }
    
    const sizeVal = sizeEl.value;
    const colorVal = colorEl.value;
    
    if(!sizeVal || !colorVal){
      stockEl.textContent = '0';
      stockEl.style.color = '#e74c3c';
      qtyEl.max = 0;
      addToCartBtn.disabled = true;
      addToCartBtn.textContent = 'Chọn size và màu sắc';
      return;
    }
    
    const sizeId = parseInt(sizeVal);
    const colorId = parseInt(colorVal);
    const match = (productData.variants||[]).find(v => 
      String(v.sizeId) === String(sizeId) && String(v.colorId) === String(colorId)
    );
    
    const stock = match ? (match.stock ?? 0) : 0;
    stockEl.textContent = stock;
    qtyEl.max = Math.max(0, stock);
    
    if(parseInt(qtyEl.value) > stock){ 
      qtyEl.value = Math.max(1, stock); 
    }
    
    // Update stock display color and button state
    if (stock <= 0) {
      stockEl.style.color = '#e74c3c'; // Red color for out of stock
      stockEl.textContent = 'Hết hàng';
      addToCartBtn.disabled = true;
      addToCartBtn.textContent = 'Hết hàng';
    } else {
      stockEl.style.color = '#27ae60'; // Green color for in stock
      addToCartBtn.disabled = false;
      addToCartBtn.textContent = 'Thêm vào giỏ hàng';
    }
  }

  function loadProductData() {
    // Get product ID from URL or data attribute
    const productId = window.location.pathname.split('/').pop();
    if (!productId) return;

    console.log('Loading product data for ID:', productId);

    // Fetch product data
    fetch('/api/products/' + productId + '/quick-view')
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(response => {
        console.log('Product data response:', response);
        if (response.success) {
          const data = response.data;
          productData = data;
          
          console.log('Product data loaded:', data);
          console.log('Variants:', data.variants);
          console.log('Sizes:', data.sizes);
          console.log('Colors:', data.colors);
          
          // Populate sizes and colors
          allSizes = data.sizes || [];
          allColors = data.colors || [];
          populateSizes(allSizes, '');
          populateColors(allColors, '');
          
          // Initial stock update
          updateStock();
        } else {
          console.error('Failed to load product data:', response.message);
        }
      })
      .catch(error => {
        console.error('Error loading product data:', error);
      });
  }

  // Event listeners
  sizeEl.addEventListener('change', function(){
    if(!productData){ return; }
    updateStock();
  });
  
  colorEl.addEventListener('change', function(){
    if(!productData){ return; }
    updateStock();
  });

  // jQuery fallback for niceSelect-triggered events
  if (window.jQuery) {
    jQuery('#sizeSelect').off('change.pd').on('change.pd', function(){
      if(!productData){ return; }
      updateStock();
    });
    jQuery('#colorSelect').off('change.pd').on('change.pd', function(){
      if(!productData){ return; }
      updateStock();
    });
  }

  // Add to cart functionality
  addToCartBtn.addEventListener('click', function() {
    if (!productData) {
      alert('Vui lòng chọn sản phẩm');
      return;
    }

    const sizeId = sizeEl.value;
    const colorId = colorEl.value;
    const quantity = parseInt(qtyEl.value);

    if (!sizeId || !colorId) {
      alert('Vui lòng chọn kích thước và màu sắc');
      return;
    }

    if (quantity <= 0) {
      alert('Số lượng phải lớn hơn 0');
      return;
    }

    // Kiểm tra tồn kho trước khi thêm vào giỏ hàng
    const stock = parseInt(stockEl.textContent);
    if (stock <= 0) {
      alert('Sản phẩm này hiện tại đã hết hàng');
      return;
    }

    // Tìm biến thể sản phẩm phù hợp
    console.log('Looking for variant with sizeId:', sizeId, 'colorId:', colorId);
    console.log('Available variants:', productData.variants);
    
    const variant = productData.variants.find(v => 
      String(v.sizeId) === String(sizeId) && String(v.colorId) === String(colorId)
    );

    console.log('Found variant:', variant);

    if (!variant) {
      // Kiểm tra xem có phải sản phẩm không có variant không
      if (!productData.variants || productData.variants.length === 0) {
        alert('Sản phẩm này không có biến thể. Vui lòng liên hệ cửa hàng để được hỗ trợ.');
        return;
      }
      alert('Không tìm thấy biến thể sản phẩm với size và màu đã chọn');
      return;
    }

    if (quantity > variant.stock) {
      alert('Số lượng vượt quá tồn kho');
      return;
    }

    // Gửi request add to cart
    const addToCartData = {
      productId: variant.id, // Sử dụng ID của biến thể, không phải sản phẩm
      quantity: quantity
    };

    console.log('Sending add to cart data:', addToCartData);
    
    fetch('/api/cart/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(addToCartData)
    })
    .then(response => {
      console.log('Response status:', response.status);
      return response.json();
    })
    .then(data => {
      console.log('Response data:', data);
      if (data.success) {
        alert(data.message);
        // Cập nhật số lượng giỏ hàng
        updateCartCount();
      } else {
        // Kiểm tra nếu là lỗi authentication
        if (data.message && data.message.includes('authentication') || data.message.includes('đăng nhập')) {
          alert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng');
          // Redirect to login page
          window.location.href = '/login';
        } else {
          alert(data.message || 'Có lỗi xảy ra');
        }
      }
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Có lỗi xảy ra khi thêm vào giỏ hàng');
    });
  });

  // Function to update cart count
  function updateCartCount() {
    fetch('/api/cart/count')
      .then(response => response.json())
      .then(data => {
        // Cập nhật hiển thị số lượng giỏ hàng nếu có
        const cartCountElement = document.querySelector('.cart_count');
        if (cartCountElement && data.success) {
          cartCountElement.textContent = data.data || 0;
        }
      })
      .catch(error => {
        console.error('Error updating cart count:', error);
      });
  }


  // Load related products
  function loadRelatedProducts() {
    const productId = window.location.pathname.split('/').pop();
    if (!productId) return;

    console.log('Loading related products for ID:', productId);

    fetch('/api/products/' + productId + '/related?limit=8')
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(response => {
        console.log('Related products response:', response);
        if (response.success && response.data.products) {
          populateRelatedProducts(response.data.products);
        } else {
          console.log('No related products found');
          document.getElementById('relatedProductsContainer').innerHTML = '<div class="col-12 text-center"><p>Không có sản phẩm liên quan</p></div>';
        }
      })
      .catch(error => {
        console.error('Error loading related products:', error);
        document.getElementById('relatedProductsContainer').innerHTML = '<div class="col-12 text-center"><p>Không thể tải sản phẩm liên quan</p></div>';
      });
  }

  function loadUpsellProducts() {
    const productId = window.location.pathname.split('/').pop();
    if (!productId) return;

    console.log('Loading upsell products for ID:', productId);

    fetch('/api/products/' + productId + '/upsell?limit=8')
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(response => {
        console.log('Upsell products response:', response);
        if (response.success && response.data.products) {
          populateUpsellProducts(response.data.products);
        } else {
          console.log('No upsell products found');
          document.getElementById('upsellProductsContainer').innerHTML = '<div class="col-12 text-center"><p>Không có sản phẩm nâng cấp</p></div>';
        }
      })
      .catch(error => {
        console.error('Error loading upsell products:', error);
        document.getElementById('upsellProductsContainer').innerHTML = '<div class="col-12 text-center"><p>Không thể tải sản phẩm nâng cấp</p></div>';
      });
  }

  let currentIndex = 0;
  let relatedProductsData = [];
  const visibleProducts = 4;
  let isAnimating = false;

  function populateRelatedProducts(products) {
    const container = document.getElementById('relatedProductsContainer');
    container.innerHTML = '';

    if (!products || products.length === 0) {
      container.innerHTML = '<div class="text-center"><p>Không có sản phẩm liên quan</p></div>';
      return;
    }

    relatedProductsData = products;
    currentIndex = 0;

    // Create carousel structure with individual product slides
    const carouselHTML = `
      <div class="related-products-carousel">
        <div class="carousel-container">
          <button class="carousel-btn prev-btn" onclick="previousProduct()" ${products.length <= visibleProducts ? 'style="display:none;"' : ''}>
            <i class="fa fa-chevron-left"></i>
          </button>
          <div class="products-display">
            <div class="slider-wrapper">
              <div class="slider-container" id="sliderContainer">
                ${getAllProductsHTML()}
              </div>
            </div>
          </div>
          <button class="carousel-btn next-btn" onclick="nextProduct()" ${products.length <= visibleProducts ? 'style="display:none;"' : ''}>
            <i class="fa fa-chevron-right"></i>
          </button>
        </div>
      </div>
    `;

    container.innerHTML = carouselHTML;

    // Add CSS styles
    addCarouselStyles();
    
    // Initialize slider position
    updateSliderPosition();
  }

  let upsellCurrentIndex = 0;
  let upsellProductsData = [];
  const upsellVisibleProducts = 4;
  let upsellIsAnimating = false;

  function populateUpsellProducts(products) {
    const container = document.getElementById('upsellProductsContainer');
    container.innerHTML = '';

    if (!products || products.length === 0) {
      container.innerHTML = '<div class="text-center"><p>Không có sản phẩm nâng cấp</p></div>';
      return;
    }

    upsellProductsData = products;
    upsellCurrentIndex = 0;

    // Create carousel structure with individual product slides (same as related products)
    const carouselHTML = `
      <div class="upsell-products-carousel">
        <div class="carousel-container">
          <button class="carousel-btn prev-btn" onclick="previousUpsellProduct()" ${products.length <= upsellVisibleProducts ? 'style="display:none;"' : ''}>
            <i class="fa fa-chevron-left"></i>
          </button>
          <div class="products-display">
            <div class="slider-wrapper">
              <div class="slider-container" id="upsellSliderContainer">
                ${getAllUpsellProductsHTML()}
              </div>
            </div>
          </div>
          <button class="carousel-btn next-btn" onclick="nextUpsellProduct()" ${products.length <= upsellVisibleProducts ? 'style="display:none;"' : ''}>
            <i class="fa fa-chevron-right"></i>
          </button>
        </div>
      </div>
    `;

    container.innerHTML = carouselHTML;

    // Add CSS styles for upsell carousel
    addUpsellCarouselStyles();
    
    // Initialize slider position
    updateUpsellSliderPosition();
  }

  function getAllProductsHTML() {
    let html = '';
    
    // Create individual slides for each product
    relatedProductsData.forEach((product, index) => {
      html += `
        <div class="slider-page">
          <div class="related-products-grid">
            ${createProductHTML(product)}
          </div>
        </div>
      `;
    });
    
    return html;
  }

  function getAllUpsellProductsHTML() {
    let html = '';
    
    // Create individual slides for each upsell product
    upsellProductsData.forEach((product, index) => {
      html += `
        <div class="slider-page">
          <div class="related-products-grid">
            ${createProductHTML(product)}
          </div>
        </div>
      `;
    });
    
    return html;
  }



  function createProductHTML(product) {
    const discountHtml = product.discount && product.discount > 0 ? 
      `<div class="product_sale"><span>-${product.discount}%</span></div>` : '';

    const oldPriceHtml = product.discount && product.discount > 0 ? 
      `<span class="old_price">${formatPrice(product.price * (100 - product.discount) / 100)}</span>` : '';

    const imageSrc = product.image || '/assets/images/product-home.png';

    return `
      <div class="single_product">
        <div class="product_thumb">
          <a class="primary_img" href="/product-details/${product.id}">
            <img src="${imageSrc}" alt="${product.name}">
          </a>
          <div class="product_action">
            <div class="hover_action">
              <a href="#"><i class="fa fa-plus"></i></a>
              <div class="action_button">
                <ul>
                  <li><a title="add to cart" href="#"><i class="fa fa-shopping-basket" aria-hidden="true"></i></a></li>
                  <li><a href="#" title="Add to Wishlist"><i class="fa fa-heart-o" aria-hidden="true"></i></a></li>
                  <li><a href="#" title="Add to Compare"><i class="fa fa-sliders" aria-hidden="true"></i></a></li>
                </ul>
              </div>
            </div>
          </div>
          <div class="quick_button">
            <a href="#" class="btn-quick-view" data-product-id="${product.id}" data-bs-toggle="modal" data-bs-target="#modal_box" title="quick_view">+ xem nhanh</a>
          </div>
          ${discountHtml}
        </div>
        <div class="product_content">
          <h3><a href="/product-details/${product.id}">${product.name}</a></h3>
          <span class="current_price">${formatPrice(product.price)}</span>
          ${oldPriceHtml}
        </div>
      </div>
    `;
  }

  function addCarouselStyles() {
    if (document.getElementById('relatedProductsCarouselStyles')) return;

    const style = document.createElement('style');
    style.id = 'relatedProductsCarouselStyles';
    style.textContent = `
      .related-products-carousel {
        width: 100%;
        margin: 20px 0;
      }
      
      .carousel-container {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 20px;
        margin-bottom: 20px;
      }
      
      .carousel-btn {
        background: #333;
        color: white;
        border: none;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.3s;
        font-size: 18px;
        flex-shrink: 0;
      }
      
      .carousel-btn:hover {
        background: #555;
      }
      
      .carousel-btn:disabled {
        background: #ccc;
        cursor: not-allowed;
      }
      
      .products-display {
        flex: 1;
        overflow: hidden;
        position: relative;
      }
      
      .slider-wrapper {
        width: 100%;
        overflow: hidden;
      }
      
      .slider-container {
        display: flex;
        transition: transform 0.3s ease-in-out;
        width: 100%;
      }
      
      .slider-page {
        min-width: 25%; /* Each slide shows 1/4 of the container (1 product out of 4) */
        flex-shrink: 0;
        padding: 0 10px; /* Add horizontal spacing between products */
      }
      
      .related-products-grid {
        display: grid;
        grid-template-columns: 1fr; /* Single column for each slide */
        gap: 0;
        height: 100%;
      }
      
      @media (max-width: 1200px) {
        .slider-page {
          min-width: 33.333%; /* Show 3 products on tablet */
          padding: 0 8px; /* Slightly less padding on tablet */
        }
      }
      
      @media (max-width: 768px) {
        .slider-page {
          min-width: 50%; /* Show 2 products on mobile */
          padding: 0 6px; /* Less padding on mobile */
        }
      }
      
      @media (max-width: 480px) {
        .slider-page {
          min-width: 100%; /* Show 1 product on small mobile */
          padding: 0 5px; /* Minimal padding on small mobile */
        }
      }
      
      .related-products-grid .single_product {
        width: 100%;
        margin: 0;
      }
      
      .related-products-grid .product_thumb img {
        width: 100%;
        height: 250px;
        object-fit: cover;
      }
      
    `;
    document.head.appendChild(style);
  }

  function addUpsellCarouselStyles() {
    const style = document.createElement('style');
    style.id = 'upsellProductsCarouselStyles';
    style.textContent = `
      .upsell-products-carousel {
        width: 100%;
        margin: 20px 0;
      }
      
      .upsell-products-carousel .carousel-container {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 20px;
        margin-bottom: 20px;
      }
      
      .upsell-products-carousel .carousel-btn {
        background: #333;
        color: white;
        border: none;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.3s;
        font-size: 18px;
        flex-shrink: 0;
      }
      
      .upsell-products-carousel .carousel-btn:hover {
        background: #555;
      }
      
      .upsell-products-carousel .carousel-btn:disabled {
        background: #ccc;
        cursor: not-allowed;
      }
      
      .upsell-products-carousel .products-display {
        flex: 1;
        overflow: hidden;
        position: relative;
      }
      
      .upsell-products-carousel .slider-wrapper {
        width: 100%;
        overflow: hidden;
      }
      
      .upsell-products-carousel .slider-container {
        display: flex;
        transition: transform 0.3s ease-in-out;
        width: 100%;
      }
      
      .upsell-products-carousel .slider-page {
        min-width: 25%; /* Each slide shows 1/4 of the container (1 product out of 4) */
        flex-shrink: 0;
        padding: 0 10px; /* Add horizontal spacing between products */
      }
      
      .upsell-products-carousel .related-products-grid {
        display: grid;
        grid-template-columns: 1fr; /* Single column for each slide */
        gap: 0;
        height: 100%;
      }
      
      @media (max-width: 1200px) {
        .upsell-products-carousel .slider-page {
          min-width: 33.333%; /* Show 3 products on tablet */
          padding: 0 8px; /* Slightly less padding on tablet */
        }
      }
      
      @media (max-width: 768px) {
        .upsell-products-carousel .slider-page {
          min-width: 50%; /* Show 2 products on mobile */
          padding: 0 6px; /* Less padding on mobile */
        }
      }
      
      @media (max-width: 480px) {
        .upsell-products-carousel .slider-page {
          min-width: 100%; /* Show 1 product on small mobile */
          padding: 0 5px; /* Minimal padding on small mobile */
        }
      }
      
      .upsell-products-carousel .related-products-grid .single_product {
        width: 100%;
        margin: 0;
      }
      
      .upsell-products-carousel .single_product .product_thumb img {
        width: 100%;
        height: 250px;
        object-fit: cover;
      }
    `;
    document.head.appendChild(style);
  }

  // Global functions for carousel navigation
  window.nextProduct = function() {
    if (isAnimating) return;
    const maxIndex = relatedProductsData.length - visibleProducts;
    if (currentIndex < maxIndex) {
      currentIndex++;
      updateSliderPosition();
    }
  };

  window.previousProduct = function() {
    if (isAnimating) return;
    if (currentIndex > 0) {
      currentIndex--;
      updateSliderPosition();
    }
  };

  // Global functions for upsell carousel navigation
  window.nextUpsellProduct = function() {
    if (upsellIsAnimating) return;
    const maxIndex = upsellProductsData.length - upsellVisibleProducts;
    if (upsellCurrentIndex < maxIndex) {
      upsellCurrentIndex++;
      updateUpsellSliderPosition();
    }
  };

  window.previousUpsellProduct = function() {
    if (upsellIsAnimating) return;
    if (upsellCurrentIndex > 0) {
      upsellCurrentIndex--;
      updateUpsellSliderPosition();
    }
  };


  function updateSliderPosition() {
    const sliderContainer = document.getElementById('sliderContainer');
    
    if (sliderContainer) {
      isAnimating = true;
      const translateX = -currentIndex * 25; // Each product is 25% width
      sliderContainer.style.transform = `translateX(${translateX}%)`;
      
      // Reset animation flag after transition
      setTimeout(() => {
        isAnimating = false;
      }, 300);
    }
    
    // Update navigation buttons
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');
    const maxIndex = relatedProductsData.length - visibleProducts;
    
    if (prevBtn) {
      prevBtn.style.display = currentIndex === 0 ? 'none' : 'flex';
    }
    if (nextBtn) {
      nextBtn.style.display = currentIndex >= maxIndex ? 'none' : 'flex';
    }
  }

  function updateUpsellSliderPosition() {
    const sliderContainer = document.getElementById('upsellSliderContainer');
    
    if (sliderContainer) {
      upsellIsAnimating = true;
      const translateX = -upsellCurrentIndex * 25; // Each product is 25% width
      sliderContainer.style.transform = `translateX(${translateX}%)`;
      
      // Reset animation flag after transition
      setTimeout(() => {
        upsellIsAnimating = false;
      }, 300);
    }
    
    // Update navigation buttons for upsell carousel
    const upsellCarousel = document.querySelector('.upsell-products-carousel');
    if (upsellCarousel) {
      const prevBtn = upsellCarousel.querySelector('.prev-btn');
      const nextBtn = upsellCarousel.querySelector('.next-btn');
      const maxIndex = upsellProductsData.length - upsellVisibleProducts;
      
      if (prevBtn) {
        prevBtn.style.display = upsellCurrentIndex === 0 ? 'none' : 'flex';
      }
      if (nextBtn) {
        nextBtn.style.display = upsellCurrentIndex >= maxIndex ? 'none' : 'flex';
      }
    }
  }


  function formatPrice(price) {
    try {
      return new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(price) + ' VNĐ';
    } catch (e) {
      return price + ' VNĐ';
    }
  }

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
      loadProductData();
      loadRelatedProducts();
      loadUpsellProducts();
    });
  } else {
    loadProductData();
    loadRelatedProducts();
    loadUpsellProducts();
  }

  // Update cart count when page loads
  updateCartCount();
})();
