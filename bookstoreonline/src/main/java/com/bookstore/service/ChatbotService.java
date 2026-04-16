package com.bookstore.service;

import com.bookstore.dto.AiSearchResult;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.VoucherDTO;
import com.bookstore.dto.OrderResponseDTO;
import com.bookstore.dto.CategoryDTO;
import com.bookstore.dto.UserContext;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final AiSearchService aiSearchService;
    private final VoucherService voucherService;
    private final BookService bookService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final CategoryService categoryService;
    private final Map<String, UserContext> sessionMap = new ConcurrentHashMap<>();

    private static final List<String> DEFAULT_QUICK_REPLIES =
        List.of("Sách mới nhất ✨", "Sách bán chạy 🔥", "Đề xuất theo thể loại 📚", "Mã giảm giá 🎁");

    // Smart Personality Triggers
    private static final Map<String, String> SMALL_TALK = Map.ofEntries(
        Map.entry("bạn là ai", "Tôi là trợ lý ảo thông minh của Booksaw, được tạo ra để giúp bạn tìm kiếm những cuốn sách tâm đắc và hỗ trợ mua sắm nhanh chóng nhất! 🤖"),
        Map.entry("who are you", "I'm Booksaw's AI Assistant, ready to help you discover great books! 📚"),
        Map.entry("tên gì", "Bạn có thể gọi tôi là Booksaw Bot. Rất vui được hỗ trợ bạn! ✨"),
        Map.entry("khỏe không", "Tôi luôn tràn đầy năng lượng để phục vụ bạn! Hôm nay bạn muốn tìm cuốn sách nào? 😊"),
        Map.entry("yêu", "Cảm ơn bạn! Tôi cũng rất yêu mến những khách hàng yêu sách như bạn. ❤️"),
        Map.entry("ghét", "Rất tiếc nếu tôi làm bạn không hài lòng. Tôi sẽ cố gắng học hỏi để thông minh hơn! 🥺"),
        Map.entry("cảm ơn", "Không có chi! Rất sẵn lòng giúp đỡ bạn. Chúc bạn một ngày đọc sách thật vui! 🌸"),
        Map.entry("thanks", "You're very welcome! Happy reading! 📖"),
        Map.entry("tạm biệt", "Chào tạm biệt! Hy vọng sớm gặp lại bạn giữa những trang sách. 👋")
    );

    public ChatbotService(AiSearchService aiSearchService, 
                          VoucherService voucherService, 
                          BookService bookService,
                          OrderService orderService,
                          InventoryService inventoryService,
                          CategoryService categoryService) {
        this.aiSearchService = aiSearchService;
        this.voucherService = voucherService;
        this.bookService = bookService;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.categoryService = categoryService;
    }

    public Map<String, Object> getResponse(String message, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "default-session";
        }

        if (message == null || message.isBlank()) {
            return build("Bạn cần hỗ trợ gì ạ? Tôi sẵn sàng giúp bạn!", DEFAULT_QUICK_REPLIES, sessionId);
        }

        String msg = message.toLowerCase().trim();
        UserContext context = sessionMap.getOrDefault(sessionId, new UserContext());
        
        // ── IQ Improvement: Small Talk & Personality ────────────────────────
        for (Map.Entry<String, String> entry : SMALL_TALK.entrySet()) {
            if (msg.contains(entry.getKey())) {
                return build(entry.getValue(), generateSmartQuickReplies(msg, context), sessionId);
            }
        }

        // ── Greeting (Smarter) ───────────────────────────────────────────────
        if (containsAny(msg, "chào", "hello", "hi", "hey", "good morning", "good afternoon")) {
            String timeGreeting = "Chúc bạn một ngày mới tốt lành!";
            int hour = java.time.LocalTime.now().getHour();
            if (hour < 12) timeGreeting = "Chào buổi sáng! Bạn đã sẵn sàng khám phá sách mới chưa? ☕";
            else if (hour < 18) timeGreeting = "Chào buổi chiều! Một tách trà và một cuốn sách thì sao nhỉ? 🍵";
            else timeGreeting = "Chào buổi tối! Chúc bạn có những phút giây thư giãn bên trang sách. 🌙";

            return build("👋 " + timeGreeting + "\n\nTôi là Booksaw AI, tôi có thể:\n• 🔍 Tìm sách theo **tên, tác giả, thể loại**\n• 💰 Sắp xếp sách theo **giá rẻ nhất**\n• 📦 Tra cứu **đơn hàng** & **tồn kho**\n• 🎁 Tặng bạn **mã giảm giá**\n\nBạn muốn tôi giúp gì nào?",
                generateSmartQuickReplies(msg, context), sessionId);
        }

        // ── Thanks / Goodbye ──────────────────────────────────────────────────
        if (containsAny(msg, "cảm ơn", "thank", "tạm biệt", "bye", "hẹn gặp lại", "xong rồi")) {
            return build("✨ Rất vui được hỗ trợ bạn! Chúc bạn có những phút giây đọc sách thật thú vị và ý nghĩa. Hẹn gặp lại bạn sớm nhé! 👋",
                List.of("Tìm sách tiếp 📚", "Sách mới nhất ✨"), sessionId);
        }

        // ── Order / Delivery (DYNAMIC) ────────────────────────────────────────
        if (containsAny(msg, "đơn hàng của tôi", "lịch sử mua", "đã mua gì", "kiểm tra đơn")) {
            String username = sessionId; 
            if (username == null || username.equals("default-session") || username.length() > 30) {
                return build("📦 Để tra cứu đơn hàng cá nhân, bạn vui lòng **Đăng nhập** trước nhé!\nHoặc bạn có thể nhắn mã đơn (Ví dụ: ORD123) để tôi kiểm tra giúp.",
                    List.of("Đăng nhập ngay 🔑", "Tìm sách hay 📚"), sessionId);
            }

            try {
                List<OrderResponseDTO> history = orderService.getOrderHistory(username);
                if (history.isEmpty()) {
                    return build("📦 Chào " + username + ", hệ thống chưa ghi nhận đơn hàng nào của bạn. Hãy lướt xem vài cuốn sách hay để ủng hộ shop nhé!",
                        generateSmartQuickReplies(msg, context), sessionId);
                }

                StringBuilder sb = new StringBuilder();
                sb.append("📦 **Lịch sử mua hàng của bạn**:\n\n");
                int limit = Math.min(history.size(), 3);
                NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

                for (int i = 0; i < limit; i++) {
                    OrderResponseDTO o = history.get(i);
                    sb.append("🆔 **").append(o.getOrderId()).append("** [").append(o.getStatus()).append("]\n");
                    sb.append("   ▸ Tổng: ").append(cf.format(o.getTotalAmount())).append("\n\n");
                }
                sb.append("👉 Chi tiết hơn có tại mục **Tài khoản -> Đơn hàng**.");
                return build(sb.toString(), List.of("Kiểm tra mã khác 🔍", "Mua tiếp 🛒"), sessionId);
            } catch (Exception e) {
                return build("📦 Tôi gặp lỗi khi truy xuất đơn hàng. Bạn vui lòng cung cấp mã đơn cụ thể nhé!", 
                    List.of("Mã giảm giá 🎁", "Sách mới ✨"), sessionId);
            }
        }

        // ── Track by ID ──
        if (containsAny(msg, "ord", "đơn hàng ") && msg.length() > 5) {
             String orderId = message.toUpperCase().trim();
             if (orderId.startsWith("KIỂM TRA ĐƠN HÀNG ")) orderId = orderId.replace("KIỂM TRA ĐƠN HÀNG ", "");
             try {
                 OrderResponseDTO o = orderService.getOrderDetail(orderId);
                 NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
                 StringBuilder sb = new StringBuilder();
                 sb.append("🔍 **Thông tin đơn hàng ").append(o.getOrderId()).append("**:\n\n");
                 sb.append("▸ Trạng thái: **").append(o.getStatus()).append("**\n");
                 sb.append("▸ Người nhận: ").append(o.getCustomerName()).append("\n");
                 sb.append("▸ Tổng cộng: **").append(cf.format(o.getTotalAmount())).append("**\n");
                 sb.append("▸ Địa chỉ: ").append(o.getShippingAddress()).append("\n\n");
                 sb.append("Cảm ơn bạn đã ủng hộ Booksaw!");
                 return build(sb.toString(), List.of("Cảm ơn bot! 😊", "Mua tiếp 🛒"), sessionId);
             } catch (Exception e) {
                 return build("❌ Không tìm thấy đơn hàng mã **" + orderId + "**. Bạn hãy kiểm tra lại nhé!",
                     List.of("Hỗ trợ ngay 📞", "Tìm sách 📚"), sessionId);
             }
        }

        // ── Delivery / Transport Info ──
        if (containsAny(msg, "giao hàng", "ship", "vận chuyển", "bao lâu")) {
            return build("🚚 **Vận chuyển siêu tốc**:\n\n• Nội thành: 1-2 ngày\n• Toàn quốc: 3-5 ngày\n• Freeship: Đơn từ **300k**\n\nBạn có muốn lấy mã Freeship không?",
                List.of("Lấy mã Freeship 🎁", "Kiểm tra đơn 📦"), sessionId);
        }

        // ── Stock Check ──
        if (containsAny(msg, "còn hàng không", "còn không", "tồn kho")) {
            String last = context.getLastKeyword();
            if (last == null) return build("🔍 Bạn cho tôi biết tên cuốn sách bạn muốn kiểm tra nhé?", List.of("Tìm sách 📚"), sessionId);
            try {
                List<BookDTO> books = bookService.searchAndFilterBooks(last, null, null, null, null);
                if (books.isEmpty()) return build("🤔 Tôi không tìm thấy sách tên **" + last + "**.", List.of("Tìm lại 🔍"), sessionId);
                BookDTO target = books.get(0);
                com.bookstore.dto.InventoryDetailDTO stock = inventoryService.scanBarcode(target.getIsbn());
                String res = stock.getStockQuantity() > 0 ? "✅ Còn hàng (" + stock.getStockQuantity() + " cuốn)" : "❌ Hết hàng";
                return build("📦 **Tồn kho " + target.getTitle() + "**:\n\n" + res, List.of("Mua ngay 🛒", "Tìm thêm 📚"), sessionId);
            } catch (Exception e) {
                return build("📦 Hiện không thể kiểm tra kho. Vui lòng hỏi lại sau!", List.of("Hỏi khác 💬"), sessionId);
            }
        }

        // ── Category / Genres ──
        if (containsAny(msg, "thể loại", "danh mục", "đề xuất")) {
            List<CategoryDTO> cats = categoryService.getAllCategories();
            StringBuilder sb = new StringBuilder("📚 **Thể loại đa dạng tại Booksaw**:\n\n");
            cats.forEach(c -> sb.append("• ").append(c.getCategoryName()).append("\n"));
            sb.append("\nBạn thích thể loại nào? (Ví dụ: \"Sách kinh tế\")");
            List<String> qr = cats.stream().limit(3).map(c -> "Sách " + c.getCategoryName()).collect(Collectors.toList());
            return build(sb.toString(), qr, sessionId);
        }

        // ── Voucher / Promos ──
        if (containsAny(msg, "voucher", "mã giảm giá", "freeship", "khuyến mãi")) {
            List<VoucherDTO> vs = voucherService.getActiveVouchers();
            if (vs.isEmpty()) return build("🎁 Hiện không có mã mới. Đợi đợt tới nhé!", List.of("Xem sách 📚"), sessionId);
            StringBuilder sb = new StringBuilder("🎁 **Voucher & Freeship của bạn**:\n\n");
            NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            vs.forEach(v -> sb.append("🎫 **").append(v.getVoucherCode()).append("** - Giảm ").append(cf.format(v.getDiscountValue())).append("\n"));
            return build(sb.toString(), List.of("Dùng ngay 🛒", "Cảm ơn! ✨"), sessionId);
        }

        // ── Best Sellers / New Books ──
        if (containsAny(msg, "bán chạy", "mới nhất", "hot")) {
            List<BookDTO> books = msg.contains("bán chạy") ? bookService.getBestSellers() : bookService.getLatestBooks();
            if (books.isEmpty()) return build("📚 Hiện chưa có cập nhật. Thử lại sau nhé!", List.of("Tìm sách 🔍"), sessionId);
            StringBuilder sb = new StringBuilder(msg.contains("bán chạy") ? "🔥 **TOP Bán Chạy**:\n\n" : "✨ **Sách Mới Về**:\n\n");
            NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            for (int i=0; i<Math.min(books.size(), 3); i++) {
                sb.append(i+1).append(". **").append(books.get(i).getTitle()).append("** - ").append(cf.format(books.get(i).getPrice())).append("\n");
            }
            return build(sb.toString(), generateSmartQuickReplies(msg, context), sessionId);
        }

        // ── Intelligence: Semantic Search & Context Flow ────────────────────
        try {
            Thread.sleep(400); // Simulate thinking
            AiSearchResult result = aiSearchService.searchWithContext(message, context);
            List<BookDTO> found = result.getBooks();
            context.update(result.getExtractedContext());
            sessionMap.put(sessionId, context);

            if (found.isEmpty()) {
                return build("🤔 Tôi chưa tìm thấy cuốn nào phù hợp hoàn toàn.\nBạn có muốn xem thử các **Sách mới nhất** không?",
                    List.of("Sách mới nhất ✨", "Sách bán chạy 🔥"), sessionId);
            }

            StringBuilder sb = new StringBuilder("💡 **Có vẻ đây là những cuốn bạn đang tìm:**\n\n");
            NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            for (int i=0; i<Math.min(found.size(), 4); i++) {
                BookDTO b = found.get(i);
                sb.append("📖 **").append(b.getTitle()).append("**\n   ▸ Giá: ").append(cf.format(b.getPrice())).append("\n\n");
            }
            if (found.size() > 4) sb.append("... và còn nhiều kết quả khác.");
            return build(sb.toString(), generateSmartQuickReplies(msg, context), sessionId);

        } catch (Exception e) {
            return build("😅 Bộ não AI của tôi đang bảo trì chút xíu. Bạn hỏi lại nhé!", List.of("Sách mới ✨"), sessionId);
        }
    }

    private List<String> generateSmartQuickReplies(String msg, UserContext context) {
        List<String> qr = new ArrayList<>();
        if (msg.contains("mua") || msg.contains("giá")) {
            qr.add("Sách rẻ nhất 💰");
            qr.add("Mã giảm giá 🎁");
        }
        if (context.getLastKeyword() != null) {
            qr.add("Còn hàng không? 📦");
            qr.add("Thế còn phí ship? 🚚");
        }
        if (qr.size() < 2) {
            qr.add("Sách mới nhất ✨");
            qr.add("Sách bán chạy 🔥");
        }
        if (qr.size() < 4) qr.add("Đơn hàng của tôi 📦");
        return qr.stream().distinct().collect(Collectors.toList());
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private Map<String, Object> build(String message, List<String> quickReplies, String sessionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("quickReplies", quickReplies);
        result.put("sessionId", sessionId);
        return result;
    }
}
