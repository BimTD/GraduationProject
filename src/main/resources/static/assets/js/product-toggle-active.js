function toggleActiveStatus(checkbox) {
    const productId = checkbox.getAttribute('data-id');
    const isActive = checkbox.checked;

    fetch(`/admin/product/toggle-active/${productId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ active: isActive })
    })
    .then(response => {
        if (!response.ok) {
            alert('Status update failed!');
            checkbox.checked = !isActive; // revert lại nếu lỗi
        }
    })
    .catch(() => {
        alert('An error occurred!');
        checkbox.checked = !isActive;
    });
} 