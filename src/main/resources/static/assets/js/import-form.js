// Import Form JavaScript
document.addEventListener('DOMContentLoaded', function() {
    let currentStep = 1;
    let selectedSupplier = null;
    let importDetails = [];
    let products = [];
    let variants = [];

    initForm();

    function initForm() {
        // Lấy dữ liệu sản phẩm và biến thể từ HTML
        loadProductsAndVariants();
        setupEventListeners();
        updateNavigationButtons();
    }

    function loadProductsAndVariants() {
        // Lấy danh sách sản phẩm từ select
        const productSelect = document.getElementById('productSelect');
        if (productSelect) {
            products = Array.from(productSelect.options).map(option => ({
                id: option.value,
                name: option.textContent,
                importPrice: option.dataset.importPrice || '0'
            }));
        }

        // Lấy danh sách biến thể từ HTML (sẽ được cập nhật động)
        const variantSelect = document.getElementById('variantSelect');
        if (variantSelect) {
            variants = [];
        }
    }

    function setupEventListeners() {
        // Supplier selection
        const supplierCards = document.querySelectorAll('.supplier-card');
        supplierCards.forEach(card => {
            card.addEventListener('click', () => selectSupplier(card));
        });

        // Supplier search
        const supplierSearch = document.getElementById('supplierSearch');
        if (supplierSearch) {
            supplierSearch.addEventListener('input', (e) => filterSuppliers(e.target.value));
        }

        // Navigation buttons
        const nextBtn = document.getElementById('nextBtn');
        if (nextBtn) {
            nextBtn.addEventListener('click', nextStep);
        }

        const prevBtn = document.getElementById('prevBtn');
        if (prevBtn) {
            prevBtn.addEventListener('click', prevStep);
        }

        const nextBtnStep2 = document.getElementById('nextBtnStep2');
        if (nextBtnStep2) {
            nextBtnStep2.addEventListener('click', nextStep);
        }

        // Product selection
        const productSelect = document.getElementById('productSelect');
        if (productSelect) {
            productSelect.addEventListener('change', handleProductChange);
        }

        // Variant selection
        const variantSelect = document.getElementById('variantSelect');
        if (variantSelect) {
            variantSelect.addEventListener('change', handleVariantChange);
        }

        // Quantity input
        const importQuantity = document.getElementById('importQuantity');
        if (importQuantity) {
            importQuantity.addEventListener('input', validateForm);
        }

        // Add detail button
        const addDetailBtn = document.getElementById('addDetailBtn');
        if (addDetailBtn) {
            addDetailBtn.addEventListener('click', addImportDetail);
        }

        // Previous button for step 3
        const prevBtnStep3 = document.getElementById('prevBtnStep3');
        if (prevBtnStep3) {
            prevBtnStep3.addEventListener('click', prevStep);
        }

        // Complete button
        const completeBtn = document.getElementById('completeBtn');
        if (completeBtn) {
            completeBtn.addEventListener('click', completeImport);
        }
    }

    function handleProductChange() {
        const productSelect = document.getElementById('productSelect');
        const variantSelect = document.getElementById('variantSelect');
        const importPrice = document.getElementById('importPrice');
        
        if (productSelect.value) {
            // Lấy thông tin sản phẩm được chọn
            const selectedProduct = products.find(p => p.id === productSelect.value);
            if (selectedProduct) {
                // Cập nhật giá nhập
                const price = parseFloat(selectedProduct.importPrice);
                importPrice.value = formatCurrency(price);
                
                // Cập nhật danh sách biến thể
                updateVariants(selectedProduct.id);
                
                // Enable variant select
                variantSelect.disabled = false;
            }
        } else {
            // Reset form
            variantSelect.innerHTML = '<option value="">Chọn biến thể...</option>';
            variantSelect.disabled = true;
            importPrice.value = '';
            importQuantity.value = '';
        }
        
        validateForm();
    }

    function updateVariants(productId) {
        const variantSelect = document.getElementById('variantSelect');
        
        // Lấy tất cả biến thể của sản phẩm này từ phần ẩn
        const productVariants = Array.from(document.querySelectorAll(`#productVariantsData [data-product-id="${productId}"]`));
        
        variantSelect.innerHTML = '<option value="">Chọn biến thể...</option>';
        
        if (productVariants.length > 0) {
            productVariants.forEach(variant => {
                const option = document.createElement('option');
                option.value = variant.dataset.variantId;
                option.textContent = `${variant.dataset.colorName} - ${variant.dataset.sizeName}`;
                option.dataset.colorName = variant.dataset.colorName;
                option.dataset.sizeName = variant.dataset.sizeName;
                variantSelect.appendChild(option);
            });
        } else {
            // Nếu không có biến thể, tạo option mặc định
            const option = document.createElement('option');
            option.value = 'default';
            option.textContent = 'Không có biến thể';
            variantSelect.appendChild(option);
        }
    }

    function handleVariantChange() {
        validateForm();
    }

    function validateForm() {
        const productSelect = document.getElementById('productSelect');
        const variantSelect = document.getElementById('variantSelect');
        const importQuantity = document.getElementById('importQuantity');
        const addDetailBtn = document.getElementById('addDetailBtn');
        const nextBtnStep2 = document.getElementById('nextBtnStep2');
        
        const isValid = productSelect.value && 
                       variantSelect.value && 
                       importQuantity.value && 
                       parseInt(importQuantity.value) > 0;
        
        if (addDetailBtn) {
            addDetailBtn.disabled = !isValid;
        }
        
        if (nextBtnStep2) {
            nextBtnStep2.disabled = importDetails.length === 0;
        }
    }

    function addImportDetail() {
        const productSelect = document.getElementById('productSelect');
        const variantSelect = document.getElementById('variantSelect');
        const importPrice = document.getElementById('importPrice');
        const importQuantity = document.getElementById('importQuantity');
        
        if (!productSelect.value || !variantSelect.value || !importQuantity.value) {
            return;
        }
        
        const selectedProduct = products.find(p => p.id === productSelect.value);
        const selectedVariant = variantSelect.options[variantSelect.selectedIndex];
        
        const detail = {
            id: Date.now(), // Unique ID for removal
            productId: productSelect.value,
            productName: selectedProduct.name,
            variantId: variantSelect.value,
            variantName: selectedVariant.textContent,
            importPrice: importPrice.value,
            quantity: parseInt(importQuantity.value),
            totalPrice: calculateTotalPrice(importPrice.value, importQuantity.value)
        };
        
        importDetails.push(detail);
        updateImportDetailsList();
        resetProductForm();
        validateForm();
    }

    function updateImportDetailsList() {
        const detailsList = document.getElementById('importDetailsList');
        const summarySection = document.getElementById('summarySection');
        
        if (importDetails.length === 0) {
            detailsList.innerHTML = `
                <div class="text-center text-gray-500 py-8">
                    <i class="fas fa-inbox text-4xl mb-2"></i>
                    <p>Chưa có sản phẩm nào được thêm vào phiếu nhập</p>
                </div>
            `;
            summarySection.style.display = 'none';
            return;
        }
        
        let html = '<div class="space-y-3">';
        importDetails.forEach(detail => {
            html += `
                <div class="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between">
                    <div class="flex-1">
                        <h5 class="text-lg font-semibold text-gray-800">${detail.productName}</h5>
                        <p class="text-sm text-gray-600">Biến thể: ${detail.variantName}</p>
                        <p class="text-sm text-gray-600">Giá nhập: ${detail.importPrice} | Số lượng: ${detail.quantity}</p>
                    </div>
                    <div class="flex items-center space-x-3">
                        <span class="text-lg font-semibold text-blue-600">${detail.totalPrice}</span>
                        <button type="button" class="text-red-500 hover:text-red-700" 
                                onclick="removeImportDetail(${detail.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        
        detailsList.innerHTML = html;
        
        // Hiển thị summary section và cập nhật tổng tiền
        summarySection.style.display = 'block';
        updateTotalAmount();
    }

    function removeImportDetail(detailId) {
        importDetails = importDetails.filter(detail => detail.id !== detailId);
        updateImportDetailsList();
        validateForm();
    }

    function updateTotalAmount() {
        const totalAmountElement = document.getElementById('totalAmount');
        if (totalAmountElement) {
            const total = importDetails.reduce((sum, detail) => {
                const numericPrice = parseFloat(detail.importPrice.replace(/[^\d]/g, ''));
                return sum + (numericPrice * detail.quantity);
            }, 0);
            totalAmountElement.textContent = formatCurrency(total);
        }
    }

    function resetProductForm() {
        const productSelect = document.getElementById('productSelect');
        const variantSelect = document.getElementById('variantSelect');
        const importPrice = document.getElementById('importPrice');
        const importQuantity = document.getElementById('importQuantity');
        
        productSelect.value = '';
        variantSelect.innerHTML = '<option value="">Chọn biến thể...</option>';
        variantSelect.disabled = true;
        importPrice.value = '';
        importQuantity.value = '';
        
        validateForm();
    }

    function calculateTotalPrice(price, quantity) {
        // Lấy giá trị số từ chuỗi giá tiền (loại bỏ ký tự VND, dấu phẩy, dấu chấm)
        const numericPrice = parseFloat(price.replace(/[^\d]/g, ''));
        const numericQuantity = parseInt(quantity);
        
        if (isNaN(numericPrice) || isNaN(numericQuantity)) {
            return formatCurrency(0);
        }
        
        return formatCurrency(numericPrice * numericQuantity);
    }

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    function selectSupplier(card) {
        // Remove previous selection
        document.querySelectorAll('.supplier-card').forEach(c => c.classList.remove('selected'));
        
        // Add selection to current card
        card.classList.add('selected');
        
        // Get supplier ID
        const supplierId = card.dataset.supplierId;
        selectedSupplier = supplierId;
        
        // Show supplier details
        showSupplierDetails(supplierId);
        
        // Enable next button
        const nextBtn = document.getElementById('nextBtn');
        if (nextBtn) {
            nextBtn.disabled = false;
        }
    }

    function showSupplierDetails(supplierId) {
        const card = document.querySelector(`[data-supplier-id="${supplierId}"]`);
        if (!card) return;
        
        const supplierName = card.querySelector('.supplier-name').textContent;
        const supplierEmailElement = card.querySelector('.supplier-email');
        const supplierEmail = supplierEmailElement ? supplierEmailElement.textContent : 'Không có thông tin';
        const supplierPhone = card.querySelector('.supplier-phone')?.textContent || 'Không có thông tin';
        const supplierAddress = card.querySelector('.supplier-address')?.textContent || 'Không có thông tin';

        const detailsSection = document.getElementById('supplierDetails');
        if (detailsSection) {
            detailsSection.innerHTML = `
                <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <h4 class="text-lg font-semibold text-blue-800 mb-3">Thông tin chi tiết nhà cung cấp</h4>
                    <div class="space-y-2">
                        <div class="flex items-center">
                            <i class="fas fa-building text-blue-600 w-5"></i>
                            <span class="ml-2 text-blue-700">${supplierName}</span>
                        </div>
                        <div class="flex items-center">
                            <i class="fas fa-envelope text-blue-600 w-5"></i>
                            <span class="ml-2 text-blue-700">${supplierEmail}</span>
                        </div>
                        <div class="flex items-center">
                            <i class="fas fa-phone text-blue-600 w-5"></i>
                            <span class="ml-2 text-blue-700">${supplierPhone}</span>
                        </div>
                        <div class="flex items-center">
                            <i class="fas fa-map-marker-alt text-blue-600 w-5"></i>
                            <span class="ml-2 text-blue-700">${supplierAddress}</span>
                        </div>
                    </div>
                </div>
            `;
            detailsSection.style.display = 'block';
        }
    }

    function filterSuppliers(searchTerm) {
        const supplierCards = document.querySelectorAll('.supplier-card');
        const noSuppliersMessage = document.querySelector('.no-suppliers');
        let visibleCount = 0;
        
        supplierCards.forEach(card => {
            const supplierName = card.querySelector('.supplier-name').textContent.toLowerCase();
            const supplierEmailElement = card.querySelector('.supplier-email');
            const supplierEmail = supplierEmailElement ? supplierEmailElement.textContent.toLowerCase() : '';
            
            if (supplierName.includes(searchTerm.toLowerCase()) ||
                supplierEmail.includes(searchTerm.toLowerCase())) {
                card.style.display = 'block';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });
        
        if (noSuppliersMessage) {
            if (visibleCount === 0) {
                noSuppliersMessage.style.display = 'block';
            } else {
                noSuppliersMessage.style.display = 'none';
            }
        }
    }

    function showStep(stepNumber) {
        // Hide all steps
        document.querySelectorAll('.form-step').forEach(step => {
            step.classList.remove('active');
        });
        
        // Show current step
        const currentStepElement = document.getElementById(`step${stepNumber}`);
        if (currentStepElement) {
            currentStepElement.classList.add('active');
        }
        
        currentStep = stepNumber;
    }

    function updateStepIndicator() {
        // Update step numbers
        document.querySelectorAll('.step-number').forEach((stepNum, index) => {
            if (index + 1 < currentStep) {
                stepNum.classList.remove('active');
                stepNum.classList.add('completed');
            } else if (index + 1 === currentStep) {
                stepNum.classList.remove('completed');
                stepNum.classList.add('active');
            } else {
                stepNum.classList.remove('active', 'completed');
            }
        });
        
        // Update step labels
        document.querySelectorAll('.step-label').forEach((stepLabel, index) => {
            if (index + 1 < currentStep) {
                stepLabel.classList.remove('active');
                stepLabel.classList.add('completed');
            } else if (index + 1 === currentStep) {
                stepLabel.classList.remove('completed');
                stepLabel.classList.add('active');
            } else {
                stepLabel.classList.remove('active', 'completed');
            }
        });
        
        // Update step connectors
        document.querySelectorAll('.step-connector').forEach((connector, index) => {
            if (index + 1 < currentStep) {
                connector.classList.remove('active');
                connector.classList.add('completed');
            } else if (index + 1 === currentStep) {
                connector.classList.remove('completed');
                connector.classList.add('active');
            } else {
                connector.classList.remove('active', 'completed');
            }
        });
    }

    function updateNavigationButtons() {
        // Update next button state based on current step
        if (currentStep === 1) {
            const nextBtn = document.getElementById('nextBtn');
            if (nextBtn) {
                nextBtn.disabled = !selectedSupplier;
            }
        } else if (currentStep === 2) {
            const nextBtnStep2 = document.getElementById('nextBtnStep2');
            if (nextBtnStep2) {
                nextBtnStep2.disabled = importDetails.length === 0;
            }
        }
    }

    function nextStep() {
        if (currentStep < 3) {
            currentStep++;
            showStep(currentStep);
            updateStepIndicator();
            
            if (currentStep === 2 && selectedSupplier) {
                updateSelectedSupplierInfo();
            } else if (currentStep === 3) {
                updateConfirmStep();
            }
            
            updateNavigationButtons();
        }
    }

    function prevStep() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
            updateStepIndicator();
            updateNavigationButtons();
        }
    }

    function updateSelectedSupplierInfo() {
        const card = document.querySelector(`[data-supplier-id="${selectedSupplier}"]`);
        if (!card) return;
        
        const supplierName = card.querySelector('.supplier-name').textContent;
        const supplierEmailElement = card.querySelector('.supplier-email');
        const supplierEmail = supplierEmailElement ? supplierEmailElement.textContent : 'Không có thông tin';
        const supplierPhone = card.querySelector('.supplier-phone')?.textContent || 'Không có thông tin';
        const supplierAddress = card.querySelector('.supplier-address')?.textContent || 'Không có thông tin';

        const selectedSupplierInfo = document.getElementById('selectedSupplierInfo');
        if (selectedSupplierInfo) {
            selectedSupplierInfo.innerHTML = `
                <div class="space-y-2">
                    <div class="flex items-center">
                        <i class="fas fa-building text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700 font-medium">${supplierName}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-envelope text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierEmail}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-phone text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierPhone}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-map-marker-alt text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierAddress}</span>
                    </div>
                </div>
            `;
        }
    }

    // Make functions globally accessible
    window.importForm = {
        removeImportDetail: removeImportDetail
    };

    // Hàm xử lý khi chuyển đến Bước 3
    function updateConfirmStep() {
        if (currentStep === 3) {
            updateConfirmSupplierInfo();
            updateConfirmProductsList();
            updateConfirmTotalAmount();
            updateConfirmNote();
        }
    }

    // Cập nhật thông tin nhà cung cấp trong Bước 3
    function updateConfirmSupplierInfo() {
        const card = document.querySelector(`[data-supplier-id="${selectedSupplier}"]`);
        if (!card) return;
        
        const supplierName = card.querySelector('.supplier-name').textContent;
        const supplierEmailElement = card.querySelector('.supplier-email');
        const supplierEmail = supplierEmailElement ? supplierEmailElement.textContent : 'Không có thông tin';
        const supplierPhone = card.querySelector('.supplier-phone')?.textContent || 'Không có thông tin';
        const supplierAddress = card.querySelector('.supplier-address')?.textContent || 'Không có thông tin';

        const confirmSupplierInfo = document.getElementById('confirmSupplierInfo');
        if (confirmSupplierInfo) {
            confirmSupplierInfo.innerHTML = `
                <div class="space-y-2">
                    <div class="flex items-center">
                        <i class="fas fa-building text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700 font-medium">${supplierName}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-envelope text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierEmail}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-phone text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierPhone}</span>
                    </div>
                    <div class="flex items-center">
                        <i class="fas fa-map-marker-alt text-blue-600 w-5"></i>
                        <span class="ml-2 text-blue-700">${supplierAddress}</span>
                    </div>
                </div>
            `;
        }
    }

    // Cập nhật danh sách sản phẩm trong Bước 3
    function updateConfirmProductsList() {
        const confirmProductsList = document.getElementById('confirmProductsList');
        if (!confirmProductsList) return;
        
        if (importDetails.length === 0) {
            confirmProductsList.innerHTML = '<p class="text-gray-500 text-center">Không có sản phẩm nào</p>';
            return;
        }
        
        let html = '<div class="space-y-3">';
        importDetails.forEach(detail => {
            html += `
                <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
                    <div class="flex items-center justify-between">
                        <div class="flex-1">
                            <h5 class="font-semibold text-gray-800">${detail.productName}</h5>
                            <p class="text-sm text-gray-600">Biến thể: ${detail.variantName}</p>
                            <p class="text-sm text-gray-600">Giá nhập: ${detail.importPrice} | Số lượng: ${detail.quantity}</p>
                        </div>
                        <div class="text-right">
                            <span class="text-lg font-semibold text-blue-600">${detail.totalPrice}</span>
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        
        confirmProductsList.innerHTML = html;
    }

    // Cập nhật tổng tiền trong Bước 3
    function updateConfirmTotalAmount() {
        const confirmTotalAmount = document.getElementById('confirmTotalAmount');
        if (confirmTotalAmount) {
            const total = importDetails.reduce((sum, detail) => {
                const numericPrice = parseFloat(detail.importPrice.replace(/[^\d]/g, ''));
                return sum + (numericPrice * detail.quantity);
            }, 0);
            confirmTotalAmount.textContent = formatCurrency(total);
        }
    }

    // Cập nhật ghi chú trong Bước 3
    function updateConfirmNote() {
        const importNote = document.getElementById('importNote');
        const confirmNote = document.getElementById('confirmNote');
        if (importNote && confirmNote) {
            const note = importNote.value.trim();
            confirmNote.textContent = note || 'Không có ghi chú';
        }
    }

    // Hàm hoàn thành nhập hàng
    function completeImport() {
        if (!selectedSupplier || importDetails.length === 0) {
            alert('Vui lòng chọn nhà cung cấp và thêm ít nhất một sản phẩm!');
            return;
        }

        // Hiển thị loading
        const completeBtn = document.getElementById('completeBtn');
        if (completeBtn) {
            completeBtn.disabled = true;
            completeBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        }

        // Tạo dữ liệu để gửi
        const importData = {
            supplierId: selectedSupplier,
            note: document.getElementById('importNote')?.value || '',
            details: importDetails.map(detail => ({
                productId: detail.productId,
                variantId: detail.variantId,
                quantity: detail.quantity,
                importPrice: detail.importPrice
            }))
        };

        // Gửi request tạo phiếu nhập
        fetch('/admin/import/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
            },
            body: JSON.stringify(importData)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Có lỗi xảy ra khi tạo phiếu nhập');
        })
        .then(data => {
            if (data.success) {
                alert(data.message || 'Tạo phiếu nhập thành công!');
                // Chuyển về trang danh sách phiếu nhập
                window.location.href = '/admin/import';
            } else {
                throw new Error(data.message || 'Có lỗi xảy ra khi tạo phiếu nhập');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra: ' + error.message);
        })
        .finally(() => {
            // Khôi phục button
            if (completeBtn) {
                completeBtn.disabled = false;
                completeBtn.innerHTML = '<i class="fas fa-check"></i> Hoàn thành nhập hàng';
            }
        });
    }
}); 