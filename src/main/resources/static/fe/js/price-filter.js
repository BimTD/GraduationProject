/**
 * Price Filter JavaScript
 * Handles price range slider functionality for shop page
 */
$(document).ready(function() {
    console.log('Price filter initialized');
    
    // Initialize price range slider
    var minPrice = 0;
    var maxPrice = 10000000; // 10 million VND
    
    // Get current price parameters from URL
    var urlParams = new URLSearchParams(window.location.search);
    var currentMinPrice = urlParams.get('minPrice');
    var currentMaxPrice = urlParams.get('maxPrice');
    
    if (currentMinPrice) {
        minPrice = parseInt(currentMinPrice);
    }
    if (currentMaxPrice) {
        maxPrice = parseInt(currentMaxPrice);
    }
    
    // Initialize slider
    $("#slider-range").slider({
        range: true,
        min: 0,
        max: 10000000,
        step: 100000, // 100k VND steps
        values: [minPrice, maxPrice],
        slide: function(event, ui) {
            $("#min-price").val(ui.values[0].toLocaleString('vi-VN'));
            $("#max-price").val(ui.values[1].toLocaleString('vi-VN'));
        },
        change: function(event, ui) {
            $("#min-price").val(ui.values[0].toLocaleString('vi-VN'));
            $("#max-price").val(ui.values[1].toLocaleString('vi-VN'));
        }
    });
    
    // Set initial values
    $("#min-price").val(minPrice.toLocaleString('vi-VN'));
    $("#max-price").val(maxPrice.toLocaleString('vi-VN'));
    
    // Handle form submission
    $("#price-filter-form").on("submit", function(e) {
        e.preventDefault();
        
        var form = $(this);
        var url = new URL(window.location);
        
        // Get current slider values
        var sliderValues = $("#slider-range").slider("values");
        var minVal = sliderValues[0];
        var maxVal = sliderValues[1];
        
        // Only add price parameters if they are different from default
        if (minVal > 0) {
            url.searchParams.set('minPrice', minVal);
        } else {
            url.searchParams.delete('minPrice');
        }
        
        if (maxVal < 10000000) {
            url.searchParams.set('maxPrice', maxVal);
        } else {
            url.searchParams.delete('maxPrice');
        }
        
        // Reset page to 0 when filtering
        url.searchParams.set('page', '0');
        
        // Redirect to new URL
        window.location.href = url.toString();
    });
    
    // Preserve other filter parameters when submitting price filter
    var preserveParams = ['search', 'categoryId', 'gender', 'colorId', 'tag', 'brandId', 'sort'];
    preserveParams.forEach(function(param) {
        var value = urlParams.get(param);
        if (value) {
            var input = $('<input>').attr({
                type: 'hidden',
                name: param,
                value: value
            });
            $("#price-filter-form").append(input);
        }
    });
});
