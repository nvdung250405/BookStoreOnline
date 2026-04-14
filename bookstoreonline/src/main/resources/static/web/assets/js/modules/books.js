/**
 * books.js - Book Catalog Logic
 */
const books = {
    // Fetch and display featured books on homepage
    loadFeatured: async () => {
        try {
            const result = await api.get('/books'); // Get all books
            if (result.success && result.data) {
                // Take first 4 as featured for demo
                const featured = result.data.slice(0, 4);
                books.renderGrid("#featured-books-container", featured);
            }
        } catch (error) {
            console.error("Failed to load featured books", error);
        }
    },

    // Render a list of books into a container
    renderGrid: (selector, itemList) => {
        const container = $(selector);
        if (!container.length) return;
        
        container.empty();
        
        itemList.forEach(book => {
            const html = `
                <div class="col-xl-3 col-lg-4 col-sm-6" data-aos="fade-up">
                    <div class="product-item bg-white p-3 rounded-4 shadow-sm border-0 h-100 text-center transition-all">
                        <div class="image-holder position-relative mb-4 overflow-hidden rounded-3 bg-light">
                            <img src="${book.hinhAnh || 'assets/images/product-item1.jpg'}" alt="Books" class="img-fluid product-image-bounce">
                            <button type="button" class="btn btn-accent w-100 position-absolute bottom-0 start-0 py-2 border-0 add-to-cart-hover" onclick="cart.add('${book.isbn}', 1)">
                                Thêm vào giỏ
                            </button>
                        </div>
                        <div class="product-detail">
                            <h5 class="fw-bold mb-1"><a href="javascript:void(0)" onclick="layout.render('Books', 'Details', '${book.isbn}')" class="text-decoration-none text-dark">${book.tenSach}</a></h5>
                            <p class="small text-muted mb-2">${book.tacGia || 'Đang cập nhật'}</p>
                            <div class="product-price fw-bold text-accent fs-5">${formatPrice(book.gia)}</div>
                        </div>
                    </div>
                </div>
            `;
            container.append(html);
        });
    },

    // Load individual book details
    loadDetail: async (isbn) => {
        try {
            const result = await api.get(`/books/${isbn}`);
            if (result.success && result.data) {
                const book = result.data;
                // Populating template fields (assuming they have these IDs)
                $("#book-title").text(book.tenSach);
                $("#book-price").text(formatPrice(book.gia));
                $("#book-description").text(book.moTa);
                $("#book-isbn").text(book.isbn);
                if (book.hinhAnh) $("#book-image").attr("src", book.hinhAnh);
            }
        } catch (error) {
            api.showToast("Không tìm thấy thông tin sách", "error");
        }
    }
};
