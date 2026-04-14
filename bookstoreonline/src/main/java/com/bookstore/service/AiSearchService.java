package com.bookstore.service;

import com.bookstore.dto.SachDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiSearchService {

    private final SachService sachService;

    public AiSearchService(SachService sachService) {
        this.sachService = sachService;
    }

    public List<SachDTO> searchByNaturalLanguage(String query) {
        if (query == null || query.isBlank()) {
            return sachService.searchAndFilterBooks(null, null, null, null, null, null, null);
        }

        String lowerQuery = query.toLowerCase();

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String categoryName = null;
        String keyword = null;

        // 1. RULE: Trích xuất Thể loại danh mục cứng
        String[] possibleCategories = { "thiếu nhi", "tâm lý", "tiểu thuyết", "giáo khoa",
                "kinh doanh", "công nghệ", "kinh tế", "kỹ năng",
                "văn học", "truyện tranh", "giáo trình" };
        for (String cat : possibleCategories) {
            if (lowerQuery.contains(cat)) {
                categoryName = cat;
                lowerQuery = lowerQuery.replace(cat, ""); // Loại bỏ category khỏi query để không bị dính vào keyword
                break;
            }
        }

        // 2. RULE: Kéo trần giá cao nhất
        Pattern maxPricePattern = Pattern
                .compile("(?:dưới|nhỏ hơn|rẻ hơn|tối đa|max|dưới mức|chưa tới)\\s*(\\d+)\\s*(k|nghìn|000|đ|vnd|vnđ)?");
        Matcher maxMatcher = maxPricePattern.matcher(lowerQuery);
        if (maxMatcher.find()) {
            long number = Long.parseLong(maxMatcher.group(1));
            String unit = maxMatcher.group(2);
            if (unit != null && (unit.equals("k") || unit.equals("nghìn") || unit.equals("000"))) {
                number = number * 1000;
            } else if (number < 1000 && number > 0) { // Ví dụ nói "dưới 200", thường hiểu là 200k
                number = number * 1000;
            }
            maxPrice = BigDecimal.valueOf(number);
            lowerQuery = lowerQuery.replace(maxMatcher.group(0), ""); // Tẩy vùng chữ này khỏi query
        }

        // 3. RULE: Kéo đáy giá thấp nhất
        Pattern minPricePattern = Pattern
                .compile("(?:trên|lớn hơn|từ|min|tối thiểu|mức giá từ)\\s*(\\d+)\\s*(k|nghìn|000|đ|vnd|vnđ)?");
        Matcher minMatcher = minPricePattern.matcher(lowerQuery);
        if (minMatcher.find()) {
            long number = Long.parseLong(minMatcher.group(1));
            String unit = minMatcher.group(2);
            if (unit != null && (unit.equals("k") || unit.equals("nghìn") || unit.equals("000"))) {
                number = number * 1000;
            } else if (number < 1000 && number > 0) {
                number = number * 1000;
            }
            minPrice = BigDecimal.valueOf(number);
            lowerQuery = lowerQuery.replace(minMatcher.group(0), "");
        }

        // 4. RULE: Trích xuất Nhà Xuất Bản
        String[] possibleNxbs = { "kim đồng", "trẻ", "nhã nam", "thế giới", "phụ nữ", "hội nhà văn", "thanh niên",
                "giáo dục", "tổng hợp" };
        String nxbName = null;
        for (String nxb : possibleNxbs) {
            if (lowerQuery.contains(nxb)) {
                nxbName = nxb;
                lowerQuery = lowerQuery.replace(nxb, "");
                break;
            }
        }

        // 5. RULE: Khớp số lượng trang (Max/Min)
        Integer minSoTrang = null;
        Integer maxSoTrang = null;

        Pattern maxPagePattern = Pattern.compile("(?U)(?:dưới|nhỏ hơn|mỏng|tối đa|max|ít hơn)\\s*(\\d+)\\s*(trang)?");
        Matcher maxPageMatcher = maxPagePattern.matcher(lowerQuery);
        if (maxPageMatcher.find()) {
            maxSoTrang = Integer.parseInt(maxPageMatcher.group(1));
            lowerQuery = lowerQuery.replace(maxPageMatcher.group(0), "");
        }

        Pattern minPagePattern = Pattern
                .compile("(?U)(?:trên|lớn hơn|dày|tối thiểu|min|nhiều hơn|từ)\\s*(\\d+)\\s*(trang)?");
        Matcher minPageMatcher = minPagePattern.matcher(lowerQuery);
        if (minPageMatcher.find()) {
            minSoTrang = Integer.parseInt(minPageMatcher.group(1));
            lowerQuery = lowerQuery.replace(minPageMatcher.group(0), "");
        }

        // 6. RULE: Thải loại stopwords không mang ý nghĩa tìm kiếm
        String[] stopWords = { "tìm", "cho", "tôi", "muốn", "mua", "những", "về", "chủ", "đề", "có", "sách", "quyển",
                "cuốn", "thể", "loại", "giá", "khoảng", "tiền", "thuộc", "các", "một", "nxb", "nhà", "xuất", "bản",
                "trang", "kể", "của", "là", "với", "như", "được" };
        for (String word : stopWords) {
            lowerQuery = lowerQuery.replaceAll("(?U)\\b" + word + "\\b", " "); // Thêm (?U) để hỗ trợ biên từ Unicode
                                                                               // (Tiếng Việt)
        }

        // Dọn khoảng trắng thừa
        keyword = lowerQuery.replaceAll("\\s+", " ").trim();
        // Nếu keyword quá ngắn (chỉ còn mấy chữ rời rạc vô nghĩa) hoặc rỗng thì bỏ đi
        if (keyword.length() < 2) {
            keyword = null;
        }

        // Ráp nối vào hàm Search Repo gốc
        return sachService.searchAndFilterBooks(keyword, categoryName, nxbName, minPrice, maxPrice, minSoTrang,
                maxSoTrang);
    }
}
