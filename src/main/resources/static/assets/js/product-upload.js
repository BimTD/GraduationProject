document.addEventListener('DOMContentLoaded', function () {
    const uploadInput = document.getElementById('uploadImage');
    const imageUrlsInput = document.getElementById('imageUrls');
    const previewDiv = document.getElementById('preview');
    const submitBtn = document.getElementById('submitBtn');

    let urls = [];
    let uploading = 0;

    submitBtn.disabled = true;

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
            removeBtn.title = 'Xóa ảnh này';
            removeBtn.onclick = function() {
                urls.splice(idx, 1);
                imageUrlsInput.value = urls.join(',');
                renderPreview();
                submitBtn.disabled = (urls.length === 0 || uploading > 0);
            };

            wrapper.appendChild(img);
            wrapper.appendChild(removeBtn);
            previewDiv.appendChild(wrapper);
        });
    }

    uploadInput.addEventListener('change', function (e) {
        const files = e.target.files;
        uploading = files.length;
        submitBtn.disabled = true;

        if (files.length === 0) {
            submitBtn.disabled = (urls.length === 0);
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
                        alert('Upload ảnh thất bại: ' + JSON.stringify(data));
                    }
                })
                .catch(err => {
                    alert('Lỗi upload ảnh: ' + err.message);
                })
                .finally(() => {
                    uploading--;
                    submitBtn.disabled = (urls.length === 0 || uploading > 0);
                });
        }
    });

    document.querySelector('form').addEventListener('submit', function (e) {
        if (urls.length === 0 || uploading > 0) {
            alert('Vui lòng upload ít nhất 1 ảnh trước khi gửi!');
            e.preventDefault();
            return false;
        }
    });
}); 