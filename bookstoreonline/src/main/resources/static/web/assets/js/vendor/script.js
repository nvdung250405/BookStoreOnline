/**
 * Standardized initialization for Booksaw Theme
 * Wrapped to allow re-triggering via Layout Engine (AJAX)
 */
function initBooksawTheme() {
    "use strict";

    // 1. Tabs initialization
    const tabs = document.querySelectorAll('[data-tab-target]')
    const tabContents = document.querySelectorAll('[data-tab-content]')

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const target = document.querySelector(tab.dataset.tabTarget)
            tabContents.forEach(tabContent => {
                tabContent.classList.remove('active')
            })
            tabs.forEach(tab => {
                tab.classList.remove('active')
            })
            tab.classList.add('active')
            if (target) target.classList.add('active')
        })
    });

    // 2. Responsive Navigation (Hamburger)
    const hamburger = document.querySelector(".hamburger");
    const navMenu = document.querySelector(".menu-list");

    if (hamburger) {
        hamburger.onclick = function() {
            hamburger.classList.toggle("active");
            navMenu.classList.toggle("responsive");
        }
    }

    // 3. jQuery Dependent Plugins
    if (typeof jQuery !== 'undefined') {
        const $ = jQuery;

        // Chocolat Lightbox
        if (typeof Chocolat !== 'undefined') {
            Chocolat(document.querySelectorAll('.image-link'), {
                imageSize: 'contain',
                loop: true,
            });
        }

        // Search Toggle
        $('#header-wrap').off('click', '.search-toggle').on('click', '.search-toggle', function(e) {
            var selector = $(this).data('selector');
            $(selector).toggleClass('show').find('.search-input').focus();
            $(this).toggleClass('active');
            e.preventDefault();
        });

        // Slick Slider - Billboard
        if ($.fn.slick) {
            $('.main-slider').not('.slick-initialized').slick({
                autoplay: true,
                autoplaySpeed: 4000,
                fade: true,
                dots: true,
                prevArrow: $('.prev'),
                nextArrow: $('.next'),
            });

            // Slick Slider - Product Grid
            $('.product-grid').not('.slick-initialized').slick({
                slidesToShow: 4,
                slidesToScroll: 1,
                autoplay: false,
                dots: true,
                arrows: false,
                responsive: [
                    { breakpoint: 1400, settings: { slidesToShow: 3 } },
                    { breakpoint: 999, settings: { slidesToShow: 2 } },
                    { breakpoint: 660, settings: { slidesToShow: 1 } }
                ]
            });
        }

        // AOS Animation
        if (typeof AOS !== 'undefined') {
            AOS.init({
                duration: 1200,
                once: true,
            });
        }

        // Stellarnav
        if ($.fn.stellarNav) {
            $('.stellarnav').stellarNav({
                theme: 'plain',
                closingDelay: 250
            });
        }
    }
}

// Initial trigger on full page load
document.addEventListener("DOMContentLoaded", () => {
    initBooksawTheme();
});