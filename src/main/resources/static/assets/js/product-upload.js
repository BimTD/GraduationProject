document.addEventListener('DOMContentLoaded', function () {
    const uploadInput = document.getElementById('uploadImage');
    const imageUrlsInput = document.getElementById('imageUrls');
    const previewDiv = document.getElementById('preview');
    const submitBtn = document.getElementById('submitBtn');
    const form = document.querySelector('form');

    let urls = [];
    let uploading = 0;
    let isEditMode = document.querySelector('input[name="id"]') !== null; // Check if we're in edit mode
    let nameCheckTimeout = null;
    let isNameValid = false;

    // In edit mode, don't disable submit button initially
    if (!isEditMode) {
        submitBtn.disabled = true;
    }

    // Validation functions
    function validateForm() {
        const ten = document.querySelector('input[name="ten"]').value.trim();
        const giaBan = document.querySelector('input[name="giaBan"]').value;
        const giaNhap = document.querySelector('input[name="giaNhap"]').value;
        const gioiTinh = document.querySelector('select[name="gioiTinh"]').value;
        const loaiId = document.querySelector('select[name="loaiId"]').value;
        const nhanHieuId = document.querySelector('select[name="nhanHieuId"]').value;
        const nhaCungCapId = document.querySelector('select[name="nhaCungCapId"]').value;

        let isValid = true;
        let errorMessage = '';

        // Validate tên sản phẩm
        if (!ten) {
            errorMessage += 'Tên sản phẩm là bắt buộc.\n';
            isValid = false;
        } else if (ten.length < 2) {
            errorMessage += 'Tên sản phẩm phải có ít nhất 2 ký tự.\n';
            isValid = false;
        } else if (ten.length > 100) {
            errorMessage += 'Tên sản phẩm không được vượt quá 100 ký tự.\n';
            isValid = false;
        }

        // Validate giá bán
        if (!giaBan || parseFloat(giaBan) <= 0) {
            errorMessage += 'Giá bán phải lớn hơn 0.\n';
            isValid = false;
        }

        // Validate giá nhập
        if (!giaNhap || parseFloat(giaNhap) <= 0) {
            errorMessage += 'Giá nhập phải lớn hơn 0.\n';
            isValid = false;
        }

        // Validate giá bán phải lớn hơn giá nhập
        if (giaBan && giaNhap && parseFloat(giaBan) > 0 && parseFloat(giaNhap) > 0) {
            if (parseFloat(giaBan) <= parseFloat(giaNhap)) {
                errorMessage += 'Giá bán phải lớn hơn giá nhập.\n';
                isValid = false;
            }
        }

        // Validate giới tính
        if (!gioiTinh) {
            errorMessage += 'Vui lòng chọn giới tính.\n';
            isValid = false;
        }

        // Validate danh mục
        if (!loaiId) {
            errorMessage += 'Vui lòng chọn danh mục.\n';
            isValid = false;
        }

        // Validate nhãn hiệu
        if (!nhanHieuId) {
            errorMessage += 'Vui lòng chọn nhãn hiệu.\n';
            isValid = false;
        }

        // Validate nhà cung cấp
        if (!nhaCungCapId) {
            errorMessage += 'Vui lòng chọn nhà cung cấp.\n';
            isValid = false;
        }

        // Validate hình ảnh (chỉ khi thêm mới)
        if (!isEditMode && (urls.length === 0 || uploading > 0)) {
            errorMessage += 'Vui lòng tải lên ít nhất 1 hình ảnh.\n';
            isValid = false;
        }

        // Validate tên không trùng (chỉ khi tên hợp lệ)
        if (ten && !isNameValid && !isEditMode) {
            errorMessage += 'Tên sản phẩm đã tồn tại. Vui lòng chọn tên khác.\n';
            isValid = false;
        }

        if (!isValid) {
            showValidationSummary(errorMessage);
        }

        return isValid;
    }

    // Show validation summary
    function showValidationSummary(errorMessage) {
        // Remove existing validation summary
        const existingSummary = document.querySelector('.validation-summary');
        if (existingSummary) {
            existingSummary.remove();
        }

        // Create validation summary
        const summaryDiv = document.createElement('div');
        summaryDiv.className = 'validation-summary error';
        
        const errorList = errorMessage.split('\n').filter(msg => msg.trim());
        const ul = document.createElement('ul');
        
        errorList.forEach(error => {
            const li = document.createElement('li');
            li.textContent = error;
            ul.appendChild(li);
        });
        
        summaryDiv.appendChild(ul);
        
        // Insert before form
        form.parentNode.insertBefore(summaryDiv, form);
        
        // Scroll to summary
        summaryDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    // Check product name uniqueness
    function checkProductName(name, productId = null) {
        if (!name || name.length < 2) {
            isNameValid = false;
            return;
        }

        const url = productId ? 
            `/admin/product/check-name?name=${encodeURIComponent(name)}&id=${productId}` :
            `/admin/product/check-name?name=${encodeURIComponent(name)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                isNameValid = data.available;
                updateNameValidationUI();
            })
            .catch(error => {
                console.error('Error checking product name:', error);
                isNameValid = false;
                updateNameValidationUI();
            });
    }

    // Update name validation UI
    function updateNameValidationUI() {
        const nameInput = document.querySelector('input[name="ten"]');
        const formGroup = nameInput.closest('.form-group');
        const existingError = document.querySelector('.name-error');
        
        if (existingError) {
            existingError.remove();
        }

        // Remove existing validation classes
        nameInput.classList.remove('error', 'success');
        formGroup.classList.remove('has-error', 'has-success');

        if (nameInput.value.trim()) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'name-error text-sm mt-1';
            
            if (isNameValid) {
                errorDiv.className += ' text-green-600';
                errorDiv.textContent = '✓ Tên sản phẩm có thể sử dụng';
                nameInput.classList.add('success');
                formGroup.classList.add('has-success');
            } else {
                errorDiv.className += ' text-red-600';
                errorDiv.textContent = '✗ Tên sản phẩm đã tồn tại';
                nameInput.classList.add('error');
                formGroup.classList.add('has-error');
            }
            
            formGroup.appendChild(errorDiv);
        }
    }

    // Add event listeners for validation
    const nameInput = document.querySelector('input[name="ten"]');
    if (nameInput) {
        nameInput.addEventListener('input', function() {
            clearTimeout(nameCheckTimeout);
            nameCheckTimeout = setTimeout(() => {
                const productId = document.querySelector('input[name="id"]')?.value;
                checkProductName(this.value.trim(), productId);
            }, 500); // Debounce for 500ms
        });
    }

    // Add real-time validation for other fields
    const requiredFields = [
        'input[name="giaBan"]',
        'input[name="giaNhap"]',
        'select[name="gioiTinh"]',
        'select[name="loaiId"]',
        'select[name="nhanHieuId"]',
        'select[name="nhaCungCapId"]'
    ];

    requiredFields.forEach(selector => {
        const field = document.querySelector(selector);
        if (field) {
            field.addEventListener('blur', function() {
                validateField(this);
            });
        }
    });

    // Special handling for price fields - validate both when either changes
    const giaBanField = document.querySelector('input[name="giaBan"]');
    const giaNhapField = document.querySelector('input[name="giaNhap"]');
    
    if (giaBanField) {
        giaBanField.addEventListener('input', function() {
            // Clear validation when user starts typing
            const formGroup = this.closest('.form-group');
            const existingError = formGroup.querySelector('.field-error');
            if (existingError) {
                existingError.remove();
            }
            this.classList.remove('error', 'success');
            formGroup.classList.remove('has-error', 'has-success', 'price-invalid', 'price-valid');
        });
        
        giaBanField.addEventListener('blur', function() {
            validateField(this);
            // Also validate giaNhap if it has a value
            if (giaNhapField && giaNhapField.value.trim()) {
                validateField(giaNhapField);
            }
        });
    }
    
    if (giaNhapField) {
        giaNhapField.addEventListener('input', function() {
            // Clear validation when user starts typing
            const formGroup = this.closest('.form-group');
            const existingError = formGroup.querySelector('.field-error');
            if (existingError) {
                existingError.remove();
            }
            this.classList.remove('error', 'success');
            formGroup.classList.remove('has-error', 'has-success', 'price-invalid', 'price-valid');
        });
        
        giaNhapField.addEventListener('blur', function() {
            validateField(this);
            // Also validate giaBan if it has a value
            if (giaBanField && giaBanField.value.trim()) {
                validateField(giaBanField);
            }
        });
    }

    function validateField(field) {
        const value = field.value.trim();
        const fieldName = field.getAttribute('name');
        const formGroup = field.closest('.form-group');
        let isValid = true;
        let errorMessage = '';

        switch(fieldName) {
            case 'giaBan':
                if (!value || parseFloat(value) <= 0) {
                    isValid = false;
                    errorMessage = 'Giá bán phải lớn hơn 0';
                } else {
                    // Kiểm tra giá bán có lớn hơn giá nhập không
                    const giaNhapValue = document.querySelector('input[name="giaNhap"]').value;
                    if (giaNhapValue && parseFloat(giaNhapValue) > 0 && parseFloat(value) <= parseFloat(giaNhapValue)) {
                        isValid = false;
                        errorMessage = 'Giá bán phải lớn hơn giá nhập';
                    }
                }
                break;
            case 'giaNhap':
                if (!value || parseFloat(value) <= 0) {
                    isValid = false;
                    errorMessage = 'Giá nhập phải lớn hơn 0';
                } else {
                    // Kiểm tra giá nhập có nhỏ hơn giá bán không
                    const giaBanValue = document.querySelector('input[name="giaBan"]').value;
                    if (giaBanValue && parseFloat(giaBanValue) > 0 && parseFloat(giaBanValue) <= parseFloat(value)) {
                        isValid = false;
                        errorMessage = 'Giá nhập phải nhỏ hơn giá bán';
                    }
                }
                break;
            case 'gioiTinh':
            case 'loaiId':
            case 'nhanHieuId':
            case 'nhaCungCapId':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Vui lòng chọn một tùy chọn';
                }
                break;
        }

        // Remove existing error and validation classes
        const existingError = formGroup.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        
        field.classList.remove('error', 'success');
        formGroup.classList.remove('has-error', 'has-success', 'price-invalid', 'price-valid');

        // Add validation classes and error if invalid
        if (!isValid) {
            field.classList.add('error');
            formGroup.classList.add('has-error');
            
            // Special styling for price comparison errors
            if (fieldName === 'giaBan' || fieldName === 'giaNhap') {
                formGroup.classList.add('price-invalid');
                formGroup.classList.remove('price-valid');
            }
            
            const errorDiv = document.createElement('div');
            errorDiv.className = 'field-error text-sm text-red-600 mt-1';
            errorDiv.textContent = errorMessage;
            formGroup.appendChild(errorDiv);
        } else if (value) {
            field.classList.add('success');
            formGroup.classList.add('has-success');
            
            // Special styling for price comparison success
            if (fieldName === 'giaBan' || fieldName === 'giaNhap') {
                formGroup.classList.add('price-valid');
                formGroup.classList.remove('price-invalid');
            }
        }
    }

    function renderPreview() {
        previewDiv.innerHTML = '';
        urls.forEach((url, idx) => {
            const wrapper = document.createElement('div');
            wrapper.className = 'img-wrapper';

            const img = document.createElement('img');
            img.src = url;

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'remove-btn';
            removeBtn.innerText = '✕';
            removeBtn.title = 'Delete this photo';
            removeBtn.onclick = function() {
                urls.splice(idx, 1);
                imageUrlsInput.value = urls.join(',');
                renderPreview();
                if (!isEditMode) {
                    submitBtn.disabled = (urls.length === 0 || uploading > 0);
                }
            };

            wrapper.appendChild(img);
            wrapper.appendChild(removeBtn);
            previewDiv.appendChild(wrapper);
        });
    }

    // Handle existing image removal in edit mode
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('remove-existing-image')) {
            const imageId = e.target.getAttribute('data-image-id');
            const imageContainer = e.target.closest('.relative');
            
            if (confirm('Bạn có chắc chắn muốn xóa ảnh này?')) {
                fetch(`/admin/product/delete-image/${imageId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                })
                .then(response => response.text())
                .then(result => {
                    if (result === 'success') {
                        imageContainer.remove();
                    } else {
                        alert('Lỗi khi xóa ảnh: ' + result);
                    }
                })
                .catch(error => {
                    alert('Lỗi khi xóa ảnh: ' + error.message);
                });
            }
        }
    });

    uploadInput.addEventListener('change', function (e) {
        const files = e.target.files;
        uploading = files.length;
        
        if (!isEditMode) {
            submitBtn.disabled = true;
        }

        if (files.length === 0) {
            if (!isEditMode) {
                submitBtn.disabled = (urls.length === 0);
            }
            return;
        }

        for (let i = 0; i < files.length; i++) {
            const formData = new FormData();
            formData.append('file', files[i]);
            formData.append('upload_preset', 'clothes');
            formData.append('folder', 'clothes/images');

            fetch('https://api.cloudinary.com/v1_1/dwl5szefn/image/upload', {
                method: 'POST',
                body: formData
            })
                .then(res => res.json())
                .then(data => {
                    if (data.secure_url) {
                        urls.push(data.secure_url);
                        imageUrlsInput.value = urls.join(',');
                        renderPreview();
                    } else {
                        alert('Upload photo failed: ' + JSON.stringify(data));
                    }
                })
                .catch(err => {
                    alert('Error uploading image: ' + err.message);
                })
                .finally(() => {
                    uploading--;
                    if (!isEditMode) {
                        submitBtn.disabled = (urls.length === 0 || uploading > 0);
                    }
                });
        }
    });

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        
        // Validate form before submission
        if (!validateForm()) {
            return false;
        }
        
        // In edit mode, allow submission even without new images
        if (!isEditMode && (urls.length === 0 || uploading > 0)) {
            alert('Vui lòng tải lên ít nhất 1 hình ảnh trước khi gửi!');
            return false;
        }
        
        // If validation passes, submit the form
        this.submit();
    });
}); 