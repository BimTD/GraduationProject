/**
 * Sort Filter JavaScript
 * Handles sorting functionality for shop page
 */
$(document).ready(function() {
    console.log('Sort filter initialized');
    
    // Get current sort parameter from URL
    var urlParams = new URLSearchParams(window.location.search);
    var currentSort = urlParams.get('sort');
    
    // Set current sort value in select
    if (currentSort) {
        $('#short').val(currentSort);
    }
    
    // Handle sort form submission
    $('#sort-form').on('change', function(e) {
        e.preventDefault();
        
        var selectedSort = $('#short').val();
        var url = new URL(window.location);
        
        // Update sort parameter
        if (selectedSort && selectedSort.trim() !== '') {
            url.searchParams.set('sort', selectedSort);
        } else {
            url.searchParams.delete('sort');
        }
        
        // Reset page to 0 when sorting
        url.searchParams.set('page', '0');
        
        // Redirect to new URL
        window.location.href = url.toString();
    });
    
    // Preserve other filter parameters when sorting
    var preserveParams = ['search', 'categoryId', 'gender', 'colorId', 'tag', 'brandId', 'minPrice', 'maxPrice'];
    preserveParams.forEach(function(param) {
        var value = urlParams.get(param);
        if (value) {
            var input = $('<input>').attr({
                type: 'hidden',
                name: param,
                value: value
            });
            $('#sort-form').append(input);
        }
    });
});

