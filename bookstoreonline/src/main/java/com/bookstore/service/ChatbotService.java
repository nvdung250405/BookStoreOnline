package com.bookstore.service;

import com.bookstore.dto.AiSearchResult;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.UserContext;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final AiSearchService aiSearchService;
    private final Map<String, UserContext> sessionMap = new ConcurrentHashMap<>();

    // Quick replies to suggest after each response
    private static final List<String> DEFAULT_QUICK_REPLIES =
        List.of("Tìm sách hay 📚", "Kiểm tra đơn hàng 📦", "Mã giảm giá 🎁", "Thanh toán 💳");

    public ChatbotService(AiSearchService aiSearchService) {
        this.aiSearchService = aiSearchService;
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

        // ── Greeting ─────────────────────────────────────────────────────────
        if (containsAny(msg, "chào", "hello", "hi", "xin chào", "hey", "good morning", "good afternoon")) {
            return build("👋 Chào bạn! Tôi là trợ lý AI siêu thông minh của **Booksaw**.\n\nTôi có thể nhạy bén hiểu được:\n• Tư vấn sách theo chủ đề, tầm giá tự nhiên\n• Kiểm tra các thông tin chính sách của cửa hàng\n• Giúp bạn đặt hàng nhanh chóng\n\nBạn muốn tìm sách gì hôm nay? (Ví dụ: \"tìm sách kinh tế dưới 200k\")",
                List.of("Tư vấn sách 📚", "Đơn hàng của tôi 📦", "Mã giảm giá 🎁", "Chính sách đổi trả 🔄"), sessionId);
        }

        // ── Thanks / Goodbye ──────────────────────────────────────────────────
        if (containsAny(msg, "cảm ơn", "camon", "thanks", "thank you", "tạm biệt", "bye", "goodbye")) {
            sessionMap.remove(sessionId); // Clear memory on goodbye
            return build("😊 Không có gì ạ! Rất vui được hỗ trợ bạn.\nNếu cần thêm gì, bạn cứ nhắn nhé. Chúc bạn đọc sách vui vẻ! 📖",
                List.of("Tìm sách tiếp 📚", "Quay lại sau 👋"), sessionId);
        }

        // ── Order / Delivery ──────────────────────────────────────────────────
        if (containsAny(msg, "đơn hàng", "order", "mua rồi", "đã đặt", "trạng thái đơn")) {
            return build("📦 **Kiểm tra đơn hàng**:\n\n1. Đăng nhập tài khoản\n2. Vào **Tài khoản → Lịch sử đơn hàng**\n3. Hoặc nhấn nút bên dưới để tra cứu ngay\n\nNếu bạn chưa thấy đơn, hãy đợi 5-10 phút sau khi đặt nhé!",
                List.of("Xem đơn hàng 📋", "Tra cứu vận đơn 🚚", "Liên hệ hỗ trợ 📞"), sessionId);
        }

        if (containsAny(msg, "giao hàng", "ship", "vận chuyển", "khi nào nhận", "bao lâu", "mấy ngày")) {
            return build("🚚 **Thời gian giao hàng**:\n\n• **Nội thành HCM/HN**: 1-2 ngày\n• **Tỉnh thành khác**: 2-4 ngày\n• **Vùng sâu vùng xa**: 4-7 ngày\n\n📌 Miễn phí vận chuyển cho đơn từ **300.000đ**!\n\nBạn có thể tra cứu vận đơn tại mục Dịch vụ → Tra cứu vận đơn.",
                List.of("Tra cứu vận đơn 🔍", "Đơn hàng của tôi 📦", "Hỏi thêm 💬"), sessionId);
        }

        // ── Discount / Voucher ────────────────────────────────────────────────
        if (containsAny(msg, "voucher", "mã giảm giá", "coupon", "khuyến mãi", "giảm giá", "sale", "ưu đãi", "mã code")) {
            return build("🎁 **Mã giảm giá hiện có**:\n\n🏷️ **BOOKSAW10** — Giảm 10% đơn từ 200k\n🏷️ **NEWMEMBER** — Giảm 20k cho thành viên mới\n🏷️ **FREESHIP** — Miễn phí ship đơn từ 150k\n\n💡 Áp dụng tại bước **Thanh toán** nhé!\n\n*Lưu ý: Mỗi mã chỉ dùng 1 lần/tài khoản*",
                List.of("Mua hàng ngay 🛒", "Xem sách hay 📚", "Điều kiện áp dụng ❓"), sessionId);
        }

        // ── Payment ───────────────────────────────────────────────────────────
        if (containsAny(msg, "thanh toán", "payment", "trả tiền", "chuyển khoản", "momo", "vnpay", "atm", "thẻ", "tiền mặt", "cod")) {
            return build("💳 **Phương thức thanh toán tại Booksaw**:\n\n✅ **VNPay QR** — Quét mã nhanh\n✅ **Chuyển khoản ngân hàng**\n✅ **COD** — Thanh toán khi nhận\n✅ **Thẻ ATM / Visa / Mastercard**\n\n🔒 Mọi giao dịch đều được mã hóa bảo mật SSL.",
                List.of("Thanh toán VNPay 📱", "COD là gì? ❓", "Đặt hàng luôn 🛒"), sessionId);
        }

        // ── AI Dynamic Book SEARCH (with memory) ──────────────────────────────
        try {
            // Giả lập cho AI "suy nghĩ chút"
            Thread.sleep(600); 
            
            AiSearchResult result = aiSearchService.searchWithContext(message, context);
            List<BookDTO> foundBooks = result.getBooks();
            
            // Save new context to memory
            context.update(result.getExtractedContext());
            sessionMap.put(sessionId, context);
            
            if (foundBooks != null && !foundBooks.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("💡 **Tôi đã lục tìm trong kho và ghi nhớ ý bạn... Dưới đây là những sách phù hợp nhất:**\n\n");
                
                int limit = Math.min(foundBooks.size(), 4);
                Locale locale = new Locale("vi", "VN");
                NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
                
                for (int i = 0; i < limit; i++) {
                    BookDTO b = foundBooks.get(i);
                    sb.append(i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "📚");
                    sb.append(" **").append(b.getTitle()).append("**\n");
                    sb.append("   ▸ *Tác giả:* ").append(String.join(", ", b.getAuthorNames())).append("\n");
                    sb.append("   ▸ *Giá:* **").append(currencyFormatter.format(b.getPrice())).append("**\n\n");
                }
                
                sb.append("👉 Bạn hãy nhấp vào phần Danh Mục hoặc tìm tên sách trên ô tìm kiếm để tậu ngay nhé!");
                
                // Trích xuất list category của sách để làm Quick reply
                Set<String> catSet = foundBooks.stream().map(BookDTO::getCategoryName).collect(Collectors.toSet());
                List<String> qr = catSet.stream().limit(3).map(c -> "Sách " + c + " 🔍").collect(Collectors.toList());
                if (qr.isEmpty()) qr = DEFAULT_QUICK_REPLIES;
                
                return build(sb.toString(), qr, sessionId);
            }
        } catch (Exception e) {
            // Log error
        }

        // ── Default fallback / Nothing found ──────────────────────────────────────────────────
        return build("🤔 Tôi đã lướt thư viện và suy nghĩ nhưng chưa tìm ra kết quả hợp với ý bạn lắm.\n\nTôi có thể hỗ trợ bạn về:\n• 📚 **Tư vấn sách theo thể loại (Kinh tế, Văn học...)**\n• 📦 **Đơn hàng & vận chuyển**\n• 💳 **Thanh toán**\n• 🎁 **Mã giảm giá**\n\nBạn có thể mô tả cụ thể hơn ví dụ: \"Tìm sách lịch sử dưới 150 nghìn\" được không?",
            List.of("Sách kinh doanh 💼", "Sách thiếu nhi 👶", "Sách kỹ năng 🧠", "Gặp nhân viên 📞"), sessionId);
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
