document.addEventListener('DOMContentLoaded', function () {
    const uploadInput = document.getElementById('uploadImage');
    const imageUrlsInput = document.getElementById('imageUrls');
    const previewDiv = document.getElementById('preview');
    const submitBtn = document.getElementById('submitBtn');

    let urls = [];
    let uploading = 0;
    let isEditMode = document.querySelector('input[name="id"]') !== null; // Check if we're in edit mode

    // In edit mode, don't disable submit button initially
    if (!isEditMode) {
        submitBtn.disabled = true;
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

    document.querySelector('form').addEventListener('submit', function (e) {
        // In edit mode, allow submission even without new images
        if (!isEditMode && (urls.length === 0 || uploading > 0)) {
            alert('Please upload at least 1 photo before submitting!');
            e.preventDefault();
            return false;
        }
    });
}); 