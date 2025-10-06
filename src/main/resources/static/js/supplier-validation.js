document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('supplierForm');
    if (!form) return;

    const tenInput = document.querySelector('input[name="ten"]');
    const sdtInput = document.querySelector('input[name="sdt"]');
    const diaChiInput = document.querySelector('input[name="diaChi"]');
    const thongTinInput = document.querySelector('input[name="thongTin"]');
    const submitBtn = form.querySelector('button[type="submit"]');

    let tenCheckTimeout = null;
    let sdtCheckTimeout = null;
    let isTenValid = false;
    let isSdtValid = false;
    let isEditMode = document.querySelector('input[name="id"]')?.value !== '';

    // Validation functions
    function validateForm() {
        const ten = tenInput.value.trim();
        const sdt = sdtInput.value.trim();
        const diaChi = diaChiInput.value.trim();

        let isValid = true;
        let errorMessage = '';

        // Validate tên nhà cung cấp
        if (!ten) {
            errorMessage += 'Tên nhà cung cấp là bắt buộc.\n';
            isValid = false;
        } else if (ten.length < 2) {
            errorMessage += 'Tên nhà cung cấp phải có ít nhất 2 ký tự.\n';
            isValid = false;
        } else if (ten.length > 255) {
            errorMessage += 'Tên nhà cung cấp không được vượt quá 255 ký tự.\n';
            isValid = false;
        }

        // Validate số điện thoại
        if (!sdt) {
            errorMessage += 'Số điện thoại là bắt buộc.\n';
            isValid = false;
        } else if (!isValidPhoneNumber(sdt)) {
            errorMessage += 'Số điện thoại không hợp lệ.\n';
            isValid = false;
        }

        // Validate địa chỉ
        if (!diaChi) {
            errorMessage += 'Địa chỉ là bắt buộc.\n';
            isValid = false;
        } else if (diaChi.length < 5) {
            errorMessage += 'Địa chỉ phải có ít nhất 5 ký tự.\n';
            isValid = false;
        }

        // Validate tên không trùng (chỉ khi tên hợp lệ)
        if (ten && !isTenValid) {
            errorMessage += 'Tên nhà cung cấp đã tồn tại. Vui lòng chọn tên khác.\n';
            isValid = false;
        }

        // Validate số điện thoại không trùng (chỉ khi số điện thoại hợp lệ)
        if (sdt && !isSdtValid) {
            errorMessage += 'Số điện thoại đã tồn tại. Vui lòng chọn số điện thoại khác.\n';
            isValid = false;
        }

        if (!isValid) {
            showValidationSummary(errorMessage);
        }

        return isValid;
    }

    // Validate phone number format
    function isValidPhoneNumber(phone) {
        const phoneRegex = /^[0-9+\-\s()]{10,15}$/;
        return phoneRegex.test(phone);
    }

    // Check supplier name uniqueness
    function checkSupplierName(name, supplierId = null) {
        if (!name || name.length < 2) {
            isTenValid = false;
            return;
        }

        const url = supplierId ? 
            `/admin/supplier/check-ten?ten=${encodeURIComponent(name)}&id=${supplierId}` :
            `/admin/supplier/check-ten?ten=${encodeURIComponent(name)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                isTenValid = data.available;
                updateNameValidationUI();
            })
            .catch(error => {
                console.error('Error checking supplier name:', error);
                isTenValid = false;
                updateNameValidationUI();
            });
    }

    // Check supplier phone number uniqueness
    function checkSupplierPhone(phone, supplierId = null) {
        if (!phone || !isValidPhoneNumber(phone)) {
            isSdtValid = false;
            return;
        }

        const url = supplierId ? 
            `/admin/supplier/check-sdt?sdt=${encodeURIComponent(phone)}&id=${supplierId}` :
            `/admin/supplier/check-sdt?sdt=${encodeURIComponent(phone)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                isSdtValid = data.available;
                updatePhoneValidationUI();
            })
            .catch(error => {
                console.error('Error checking supplier phone:', error);
                isSdtValid = false;
                updatePhoneValidationUI();
            });
    }

    // Update name validation UI
    function updateNameValidationUI() {
        const formGroup = tenInput.closest('.form-group');
        const existingError = formGroup.querySelector('.field-error');
        
        if (existingError) {
            existingError.remove();
        }

        // Remove existing validation classes
        tenInput.classList.remove('error', 'success');
        formGroup.classList.remove('has-error', 'has-success');

        if (tenInput.value.trim()) {
            const errorDiv = document.createElement('div');
            
            if (isTenValid) {
                errorDiv.className = 'field-error text-green-600';
                errorDiv.textContent = '✓ Tên nhà cung cấp có thể sử dụng';
                tenInput.classList.add('success');
                formGroup.classList.add('has-success');
            } else {
                errorDiv.className = 'field-error text-red-600';
                errorDiv.textContent = '✗ Tên nhà cung cấp đã tồn tại';
                tenInput.classList.add('error');
                formGroup.classList.add('has-error');
            }
            
            formGroup.appendChild(errorDiv);
        }
    }

    // Update phone validation UI
    function updatePhoneValidationUI() {
        const formGroup = sdtInput.closest('.form-group');
        const existingError = formGroup.querySelector('.field-error');
        
        if (existingError) {
            existingError.remove();
        }

        // Remove existing validation classes
        sdtInput.classList.remove('error', 'success');
        formGroup.classList.remove('has-error', 'has-success');

        if (sdtInput.value.trim()) {
            const errorDiv = document.createElement('div');
            
            if (isSdtValid) {
                errorDiv.className = 'field-error text-green-600';
                errorDiv.textContent = '✓ Số điện thoại có thể sử dụng';
                sdtInput.classList.add('success');
                formGroup.classList.add('has-success');
            } else {
                errorDiv.className = 'field-error text-red-600';
                errorDiv.textContent = '✗ Số điện thoại đã tồn tại';
                sdtInput.classList.add('error');
                formGroup.classList.add('has-error');
            }
            
            formGroup.appendChild(errorDiv);
        }
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

    // Real-time validation for individual fields
    function validateField(field) {
        const value = field.value.trim();
        const fieldName = field.getAttribute('name');
        const formGroup = field.closest('.form-group');
        let isValid = true;
        let errorMessage = '';

        switch(fieldName) {
            case 'ten':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Tên nhà cung cấp là bắt buộc';
                } else if (value.length < 2) {
                    isValid = false;
                    errorMessage = 'Tên nhà cung cấp phải có ít nhất 2 ký tự';
                } else if (value.length > 255) {
                    isValid = false;
                    errorMessage = 'Tên nhà cung cấp không được vượt quá 255 ký tự';
                }
                break;
            case 'sdt':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Số điện thoại là bắt buộc';
                } else if (!isValidPhoneNumber(value)) {
                    isValid = false;
                    errorMessage = 'Số điện thoại không hợp lệ';
                }
                break;
            case 'diaChi':
                if (!value) {
                    isValid = false;
                    errorMessage = 'Địa chỉ là bắt buộc';
                } else if (value.length < 5) {
                    isValid = false;
                    errorMessage = 'Địa chỉ phải có ít nhất 5 ký tự';
                }
                break;
        }

        // Remove existing error and validation classes
        const existingError = formGroup.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        
        field.classList.remove('error', 'success');
        formGroup.classList.remove('has-error', 'has-success');

        // Add validation classes and error if invalid
        if (!isValid) {
            field.classList.add('error');
            formGroup.classList.add('has-error');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'field-error text-red-600';
            errorDiv.textContent = errorMessage;
            formGroup.appendChild(errorDiv);
        } else if (value) {
            field.classList.add('success');
            formGroup.classList.add('has-success');
        }
    }

    // Event listeners
    if (tenInput) {
        tenInput.addEventListener('input', function() {
            clearTimeout(tenCheckTimeout);
            tenCheckTimeout = setTimeout(() => {
                const supplierId = document.querySelector('input[name="id"]')?.value;
                checkSupplierName(this.value.trim(), supplierId);
            }, 500); // Debounce for 500ms
            
            // Clear validation when user starts typing
            const formGroup = this.closest('.form-group');
            const existingError = formGroup.querySelector('.field-error');
            if (existingError) {
                existingError.remove();
            }
            this.classList.remove('error', 'success');
            formGroup.classList.remove('has-error', 'has-success');
        });

        tenInput.addEventListener('blur', function() {
            validateField(this);
        });
    }

    if (sdtInput) {
        sdtInput.addEventListener('input', function() {
            clearTimeout(sdtCheckTimeout);
            sdtCheckTimeout = setTimeout(() => {
                const supplierId = document.querySelector('input[name="id"]')?.value;
                checkSupplierPhone(this.value.trim(), supplierId);
            }, 500); // Debounce for 500ms
            
            // Clear validation when user starts typing
            const formGroup = this.closest('.form-group');
            const existingError = formGroup.querySelector('.field-error');
            if (existingError) {
                existingError.remove();
            }
            this.classList.remove('error', 'success');
            formGroup.classList.remove('has-error', 'has-success');
        });

        sdtInput.addEventListener('blur', function() {
            validateField(this);
        });
    }

    // Add validation for other required fields
    const requiredFields = [diaChiInput];
    requiredFields.forEach(field => {
        if (field) {
            field.addEventListener('blur', function() {
                validateField(this);
            });
        }
    });

    // Form submission
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        
        // Validate form before submission
        if (!validateForm()) {
            return false;
        }
        
        // If validation passes, submit the form
        this.submit();
    });
});
