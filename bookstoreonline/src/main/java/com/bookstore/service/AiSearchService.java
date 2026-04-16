package com.bookstore.service;

import com.bookstore.dto.BookDTO;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bookstore.dto.AiSearchResult;
import com.bookstore.dto.UserContext;

@Service
public class AiSearchService {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    // Vietnamese semantic keyword → category name mapping
    private static final Map<String, String> SEMANTIC_MAP = Map.ofEntries(
        // Economics / Business
        Map.entry("startup", "kinh tế"),
        Map.entry("business", "kinh tế"),
        Map.entry("rich", "kinh tế"),
        Map.entry("investment", "kinh tế"),
        Map.entry("entrepreneur", "kinh tế"),
        Map.entry("kinh doanh", "kinh tế"),
        Map.entry("đầu tư", "kinh tế"),
        Map.entry("khởi nghiệp", "kinh tế"),
        Map.entry("tài chính", "kinh tế"),
        Map.entry("làm giàu", "kinh tế"),
        Map.entry("marketing", "kinh tế"),
        Map.entry("quản trị", "kinh tế"),
        Map.entry("tiền", "kinh tế"),
        Map.entry("tỉ phú", "kinh tế"),
        Map.entry("mã chứng khoán", "kinh tế"),
        // Technology / IT
        Map.entry("programming", "công nghệ"),
        Map.entry("coding", "công nghệ"),
        Map.entry("lập trình", "công nghệ"),
        Map.entry("phần mềm", "công nghệ"),
        Map.entry("software", "công nghệ"),
        Map.entry("artificial intelligence", "công nghệ"),
        Map.entry("machine learning", "công nghệ"),
        Map.entry("ai ", "công nghệ"),
        Map.entry("data science", "công nghệ"),
        Map.entry("công nghệ thông tin", "công nghệ"),
        Map.entry("it ", "công nghệ"),
        Map.entry("máy tính", "công nghệ"),
        // Literature / Fiction
        Map.entry("novel", "văn học"),
        Map.entry("fiction", "văn học"),
        Map.entry("romance", "văn học"),
        Map.entry("tiểu thuyết", "văn học"),
        Map.entry("truyện", "văn học"),
        Map.entry("văn học", "văn học"),
        Map.entry("thơ", "văn học"),
        Map.entry("truyện ngắn", "văn học"),
        Map.entry("tình cảm", "văn học"),
        Map.entry("ngôn tình", "văn học"),
        Map.entry("trinh thám", "văn học"),
        // Self help / Personal development
        Map.entry("self help", "kỹ năng sống"),
        Map.entry("self-help", "kỹ năng sống"),
        Map.entry("motivation", "kỹ năng sống"),
        Map.entry("phát triển bản thân", "kỹ năng sống"),
        Map.entry("kỹ năng", "kỹ năng sống"),
        Map.entry("tư duy", "kỹ năng sống"),
        Map.entry("thành công", "kỹ năng sống"),
        Map.entry("leadership", "kỹ năng sống"),
        Map.entry("lãnh đạo", "kỹ năng sống"),
        Map.entry("động lực", "kỹ năng sống"),
        Map.entry("thói quen", "kỹ năng sống"),
        // Children / Education
        Map.entry("children", "thiếu nhi"),
        Map.entry("kids", "thiếu nhi"),
        Map.entry("thiếu nhi", "thiếu nhi"),
        Map.entry("trẻ em", "thiếu nhi"),
        Map.entry("giáo dục", "giáo trình"),
        Map.entry("textbook", "giáo trình"),
        Map.entry("học", "giáo trình"),
        Map.entry("ôn thi", "giáo trình"),
        // History / Culture
        Map.entry("history", "lịch sử"),
        Map.entry("lịch sử", "lịch sử"),
        Map.entry("culture", "văn hóa"),
        Map.entry("văn hóa", "văn hóa"),
        Map.entry("triết học", "triết học"),
        Map.entry("philosophy", "triết học"),
        // Science
        Map.entry("science", "khoa học"),
        Map.entry("khoa học", "khoa học"),
        Map.entry("vũ trụ", "khoa học"),
        Map.entry("tâm lý", "tâm lý học"),
        Map.entry("psychology", "tâm lý học")
    );

    // Vietnamese stopwords to remove before keyword search
    private static final String[] STOP_WORDS = {
        "find", "search", "show", "me", "want", "to", "buy", "with", "about", "topic",
        "books", "book", "category", "price", "around", "money", "from", "publisher",
        "nxb", "nhà", "xuất", "bản", "quyển", "cuốn", "thể", "loại",
        "tìm", "kiếm", "mua", "sách", "cho", "tôi", "giúp", "hay", "tốt",
        "những", "các", "loại sách", "về", "theo", "thuộc", "chủ đề",
        "gợi ý", "recommend", "muốn", "cần", "đọc", "tìm kiếm", "hộ", "giùm"
    };

    public AiSearchService(BookService bookService,
                           CategoryRepository categoryRepository,
                           PublisherRepository publisherRepository) {
        this.bookService = bookService;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
    }

    public List<BookDTO> searchByNaturalLanguage(String query) {
        return searchWithContext(query, null).getBooks();
    }

    public AiSearchResult searchWithContext(String query, UserContext context) {
        if (query == null || query.isBlank()) {
            return AiSearchResult.builder()
                    .books(bookService.searchAndFilterBooks(null, null, null, null, null))
                    .extractedContext(new UserContext())
                    .build();
        }

        String lowerQuery = query.toLowerCase().trim();

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String categoryName = null;
        String publisherName = null;
        boolean sortByPriceAsc = false;
        boolean sortByPriceDesc = false;
        Integer limit = null;

        // ── 0. Detect Sorting & Limit ────────────────────────────────────────
        if (containsAny(lowerQuery, "rẻ nhất", "giá thấp nhất", "rẻ nhất", "giá hời nhất")) {
            sortByPriceAsc = true;
        } else if (containsAny(lowerQuery, "đắt nhất", "giá cao nhất", "cao nhất", "đắt tiền nhất")) {
            sortByPriceDesc = true;
        }

        // Detect number for limit: "2 quyển", "3 cuốn"
        Pattern limitPattern = Pattern.compile("(\\d+)\\s*(?:quyển|cuốn|bản|sách)");
        Matcher limitMatcher = limitPattern.matcher(lowerQuery);
        if (limitMatcher.find()) {
            try {
                limit = Integer.parseInt(limitMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // ── 1. Semantic mapping (Vietnamese & English) ──────────────────────
        for (Map.Entry<String, String> entry : SEMANTIC_MAP.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                categoryName = entry.getValue();
                lowerQuery = lowerQuery.replace(entry.getKey(), " ");
                break;
            }
        }

        // ── 2. Match category names directly from DB ─────────────────────────
        if (categoryName == null) {
            List<String> dbCategories = categoryRepository.findAll().stream()
                    .map(cat -> cat.getCategoryName().toLowerCase())
                    .filter(cat -> cat.length() > 2)
                    .toList();

            for (String cat : dbCategories) {
                if (lowerQuery.contains(cat)) {
                    categoryName = cat;
                    lowerQuery = lowerQuery.replace(cat, " ");
                    break;
                }
            }
        }

        // ── 3. Extract price range (Vietnamese + English) ────────────────────
        // Price range: "100k - 300k", "100000 đến 300000"
        Pattern rangePattern = Pattern.compile(
            "(\\d+[.,]?\\d*)\\s*(k|nghìn)?\\s*(?:-|đến|đối|to|đến)\\s*(\\d+[.,]?\\d*)\\s*(k|nghìn)?");
        Matcher rangeMatcher = rangePattern.matcher(lowerQuery);
        if (rangeMatcher.find()) {
            minPrice = parsePrice(rangeMatcher.group(1), rangeMatcher.group(2));
            maxPrice = parsePrice(rangeMatcher.group(3), rangeMatcher.group(4));
            lowerQuery = lowerQuery.replace(rangeMatcher.group(0), " ");
        }

        // Max price: "under 100k", "dưới 100k", "rẻ hơn 200000đ"
        Pattern maxPricePattern = Pattern.compile(
            "(?:under|below|cheaper|max|dưới|rẻ hơn|tối đa|không quá|nhỏ hơn)\\s*(\\d+[.,]?\\d*)\\s*(k|thousand|nghìn|000|đ|vnd|vnđ)?");
        Matcher maxMatcher = maxPricePattern.matcher(lowerQuery);
        if (maxMatcher.find()) {
            maxPrice = parsePrice(maxMatcher.group(1), maxMatcher.group(2));
            lowerQuery = lowerQuery.replace(maxMatcher.group(0), " ");
        }

        // Min price: "trên 100k", "from 200k", "above 50000"
        Pattern minPricePattern = Pattern.compile(
            "(?:above|over|from|min|at least|trên|lớn hơn|từ|tối thiểu|ít nhất|hơn)\\s*(\\d+[.,]?\\d*)\\s*(k|thousand|nghìn|000|đ|vnd|vnđ)?");
        Matcher minMatcher = minPricePattern.matcher(lowerQuery);
        if (minMatcher.find()) {
            minPrice = parsePrice(minMatcher.group(1), minMatcher.group(2));
            lowerQuery = lowerQuery.replace(minMatcher.group(0), " ");
        }

        // ── 4. Publisher detection from DB ───────────────────────────────────
        List<String> dbPublishers = publisherRepository.findAll().stream()
                .map(p -> p.getPublisherName().toLowerCase())
                .filter(p -> p.length() > 2)
                .toList();

        for (String pub : dbPublishers) {
            if (lowerQuery.contains(pub)) {
                publisherName = pub;
                lowerQuery = lowerQuery.replace(pub, " ");
                break;
            }
        }

        // ── 5. Remove stopwords ───────────────────────────────────────────────
        for (String sw : STOP_WORDS) {
            lowerQuery = lowerQuery.replaceAll("(?i)\\b" + Pattern.quote(sw) + "\\b", " ");
        }

        // ── 6. Finalize keyword ───────────────────────────────────────────────
        String keyword = lowerQuery.replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                                   .replaceAll("\\s+", " ")
                                   .trim();
        if (keyword.length() < 2) keyword = null;

        // ── 7. MEMORY: Merge with existing context if necessary ──────────────
        // If the user says "dưới 200k" after asking for "kinh tế", we should remember "kinh tế"
        if (context != null) {
            if (categoryName == null) categoryName = context.getCategoryName();
            if (publisherName == null) publisherName = context.getPublisherName();
            if (minPrice == null && maxPrice == null && keyword == null) {
                // If query is just about something else, maybe keep price?
                // But usually price is specific to current query unless it's a follow up
            }
            if (keyword == null && (categoryName != null || publisherName != null || minPrice != null || maxPrice != null)) {
                // This is likely a refinement query
            } else if (keyword != null && categoryName == null) {
                 // Might be a new topic, but let's keep category if it's a follow-up
            }
        }

        UserContext newContext = UserContext.builder()
                .categoryName(categoryName)
                .publisherName(publisherName)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .lastKeyword(keyword)
                .build();

        List<BookDTO> books = bookService.searchAndFilterBooks(keyword, categoryName, publisherName, minPrice, maxPrice);

        // Fuzzy fallback if no books found and we have a keyword
        if (books.isEmpty() && keyword != null) {
            // Try searching only by keyword without other filters
            books = bookService.searchAndFilterBooks(keyword, null, null, null, null);
        }

        // ── 8. Sorting & Limiting ───────────────────────────────────────────
        if (sortByPriceAsc) {
            books.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        } else if (sortByPriceDesc) {
            books.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
        }

        if (limit != null && limit > 0 && books.size() > limit) {
            books = books.subList(0, limit);
        }

        return AiSearchResult.builder()
                .books(books)
                .extractedContext(newContext)
                .build();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private BigDecimal parsePrice(String numberStr, String unit) {
        if (numberStr == null) return null;
        numberStr = numberStr.replace(",", ".");
        long number;
        try {
            number = (long) Double.parseDouble(numberStr);
        } catch (NumberFormatException e) {
            return null;
        }
        if (unit != null && (unit.equals("k") || unit.equals("nghìn") || unit.equals("thousand"))) {
            number *= 1000;
        } else if (number > 0 && number < 1000) {
            number *= 1000; // Assume bare numbers like "50" mean 50,000
        }
        return BigDecimal.valueOf(number);
    }
}
