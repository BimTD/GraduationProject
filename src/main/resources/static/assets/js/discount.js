// Global variables
let currentEditId = null;
let isEditMode = false;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    updatePreview();

    // Check current URL to determine view mode
    const currentUrl = window.location.pathname;

    if (currentUrl.includes('/edit/')) {
        // We're in edit mode, show form view
        isEditMode = true;
        document.getElementById('formTitle').textContent = 'Edit Discount Code';
        document.getElementById('listView').style.display = 'none';
        document.getElementById('formView').style.display = 'block';
    } else if (currentUrl.includes('/create')) {
        // We're in create mode, show form view
        isEditMode = false;
        document.getElementById('formTitle').textContent = 'Create New Discount Code';
        document.getElementById('listView').style.display = 'none';
        document.getElementById('formView').style.display = 'block';
    } else {
        // We're in list mode, show list view
        isEditMode = false;
        document.getElementById('listView').style.display = 'block';
        document.getElementById('formView').style.display = 'none';
    }
});

// Event listeners
function initializeEventListeners() {
    // Navigation
    document.getElementById('createBtn').addEventListener('click', showCreateForm);
    document.getElementById('backToListBtn').addEventListener('click', showListView);
    document.getElementById('cancelBtn').addEventListener('click', showListView);

    // Form - let it submit normally
    document.getElementById('loaiGiamGia').addEventListener('change', toggleMaxDiscountField);

    // Preview updates
    const previewInputs = ['maGiamGia', 'loaiGiamGia', 'giaTriGiamGia', 'ngayBatDau', 'ngayKetThuc'];
    previewInputs.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener('input', updatePreview);
        }
    });
}

// Navigation functions
function showCreateForm() {
    // Navigate to create form
    window.location.href = '/admin/ma-giam-gia/create';
}

function showListView() {
    window.location.href = '/admin/ma-giam-gia/discount';
}

// Preview functions
function updatePreview() {
    const code = document.getElementById('maGiamGia').value || '-';
    const type = document.getElementById('loaiGiamGia').value;
    const value = document.getElementById('giaTriGiamGia').value || '0';
    const startDate = document.getElementById('ngayBatDau').value;
    const endDate = document.getElementById('ngayKetThuc').value;

    document.getElementById('previewCode').textContent = code;

    if (type === 'PERCENTAGE') {
        document.getElementById('previewValue').textContent = value + '%';
    } else if (type === 'FIXED') {
        document.getElementById('previewValue').textContent = new Intl.NumberFormat('vi-VN').format(value) + ' VNĐ';
    } else {
        document.getElementById('previewValue').textContent = '-';
    }

    if (startDate && endDate) {
        const start = new Date(startDate).toLocaleDateString('vi-VN');
        const end = new Date(endDate).toLocaleDateString('vi-VN');
        document.getElementById('previewDate').textContent = start + ' - ' + end;
    } else {
        document.getElementById('previewDate').textContent = '-';
    }
}

function toggleMaxDiscountField() {
    const type = document.getElementById('loaiGiamGia').value;
    const maxGroup = document.getElementById('giaTriToiDaGroup');
    const helpText = document.getElementById('giaTriHelp');

    if (type === 'PERCENTAGE') {
        maxGroup.style.display = 'block';
        helpText.textContent = 'Enter percentage discount (e.g., 10 for 10%)';
    } else if (type === 'FIXED') {
        maxGroup.style.display = 'none';
        helpText.textContent = 'Enter fixed discount amount (VNĐ)';
    } else {
        maxGroup.style.display = 'none';
        helpText.textContent = 'Enter discount value';
    }

    updatePreview();
}