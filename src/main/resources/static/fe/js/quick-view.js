(function(){
  const modal = document.getElementById('modal_box');
  const titleEl = document.getElementById('qv-title');
  const priceEl = document.getElementById('qv-price');
  const oldPriceEl = document.getElementById('qv-old-price');
  const sizeEl = document.getElementById('qv-size');
  const colorEl = document.getElementById('qv-color');
  const stockEl = document.getElementById('qv-stock');
  const qtyEl = document.getElementById('qv-qty');

  let qvData = null;
  let allSizes = [];
  let allColors = [];

  function niceSelectUpdate(){
    if (window.jQuery && jQuery.fn.niceSelect) {
      jQuery('#qv-size').niceSelect('update');
      jQuery('#qv-color').niceSelect('update');
    }
  }

  function populateSizes(list, preserveValue){
    const keep = preserveValue ? String(preserveValue) : '';
    sizeEl.innerHTML = '<option value="">Chọn size</option>';
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
    colorEl.innerHTML = '<option value="">Chọn màu</option>';
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

  function formatPrice(v){
    try{ 
      return new Intl.NumberFormat('vi-VN', { 
        minimumFractionDigits: 0,
        maximumFractionDigits: 0 
      }).format(v) + ' VNĐ'; 
    }catch(e){ 
      return v + ' VNĐ'; 
    }
  }

  function updateStock(){
    if(!qvData){ return; }
    const sizeVal = sizeEl.value;
    const colorVal = colorEl.value;
    if(!sizeVal || !colorVal){
      stockEl.textContent = '0';
      qtyEl.max = 0;
      return;
    }
    const sizeId = parseInt(sizeVal);
    const colorId = parseInt(colorVal);
    const match = (qvData.variants||[]).find(v => String(v.sizeId) === String(sizeId) && String(v.colorId) === String(colorId));
    const stock = match ? (match.stock ?? 0) : 0;
    stockEl.textContent = stock;
    qtyEl.max = Math.max(0, stock);
    if(parseInt(qtyEl.value) > stock){ qtyEl.value = Math.max(1, stock); }
  }

  document.addEventListener('click', function(e){
    const a = e.target.closest('a.btn-quick-view');
    if(!a) return;
    const productId = a.getAttribute('data-product-id');
    if(!productId) return;
    // reset
    titleEl.textContent = '';
    priceEl.textContent = '';
    oldPriceEl.style.display = 'none';
    sizeEl.innerHTML = '<option value="">Chọn size</option>';
    colorEl.innerHTML = '<option value="">Chọn màu</option>';
    stockEl.textContent = '0';
    qvData = null;
    // fetch
    fetch('/api/products/'+productId+'/quick-view')
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(data => {
        qvData = data;
        titleEl.textContent = data.name || '';
        priceEl.textContent = formatPrice(data.price || 0);
        var descEl = document.getElementById('qv-description');
        if (descEl) { descEl.textContent = data.description || ''; }
        if(data.discount){
          oldPriceEl.style.display = '';
          try{
            const old = Number(data.price) * (100 - Number(data.discount)) / 100;
            oldPriceEl.textContent = formatPrice(old);
          }catch(err){ oldPriceEl.style.display='none'; }
        }
        // options (always full list)
        allSizes = data.sizes || [];
        allColors = data.colors || [];
        populateSizes(allSizes, '');
        populateColors(allColors, '');
        // set single image
        var img = data.image || '';
        var imgEl = document.getElementById('qv-image');
        if (imgEl) { imgEl.src = img || 'assets/img/product/product4.jpg'; }
        updateStock();
      })
      .catch(() => {
        titleEl.textContent = 'Không tải được dữ liệu sản phẩm';
      });
  });

  sizeEl.addEventListener('change', function(){
    if(!qvData){ return; }
    updateStock();
  });
  colorEl.addEventListener('change', function(){
    if(!qvData){ return; }
    updateStock();
  });

  // jQuery fallback for niceSelect-triggered events
  if (window.jQuery) {
    jQuery('#qv-size').off('change.qv').on('change.qv', function(){
      if(!qvData){ return; }
      updateStock();
    });
    jQuery('#qv-color').off('change.qv').on('change.qv', function(){
      if(!qvData){ return; }
      updateStock();
    });
  }

  // Add to cart functionality
  document.getElementById('qv-add').addEventListener('click', function() {
    if (!qvData) {
      alert('Vui lòng chọn sản phẩm');
      return;
    }

    const sizeId = sizeEl.value;
    const colorId = colorEl.value;
    const quantity = parseInt(qtyEl.value);

    if (!sizeId || !colorId) {
      alert('Vui lòng chọn size và màu sắc');
      return;
    }

    if (quantity <= 0) {
      alert('Số lượng phải lớn hơn 0');
      return;
    }

    // Tìm biến thể sản phẩm phù hợp
    const variant = qvData.variants.find(v => 
      String(v.sizeId) === String(sizeId) && String(v.colorId) === String(colorId)
    );

    if (!variant) {
      alert('Không tìm thấy biến thể sản phẩm');
      return;
    }

    if (quantity > variant.stock) {
      alert('Số lượng vượt quá tồn kho');
      return;
    }

    // Gửi request add to cart
    const addToCartData = {
      productId: variant.id, // Sử dụng ID của biến thể, không phải sản phẩm
      sizeId: parseInt(sizeId),
      colorId: parseInt(colorId),
      quantity: quantity
    };

    fetch('/api/cart/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(addToCartData)
    })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        alert(data.message);
        // Đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('modal_box'));
        if (modal) {
          modal.hide();
        }
        // Cập nhật số lượng giỏ hàng
        updateCartCount();
      } else {
        alert(data.message || 'Có lỗi xảy ra');
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
        if (cartCountElement) {
          cartCountElement.textContent = data.count;
        }
      })
      .catch(error => {
        console.error('Error updating cart count:', error);
      });
  }

  // Update cart count when page loads
  updateCartCount();
})();


