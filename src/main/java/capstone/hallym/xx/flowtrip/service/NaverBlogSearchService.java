package capstone.hallym.xx.flowtrip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import capstone.hallym.xx.flowtrip.dto.BlogReviewDto;

@Service
public class NaverBlogSearchService {

    @Value("${naver.search.client-id}")
    private String clientId;

    @Value("${naver.search.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final long CACHE_TTL_MILLIS = 1000L * 60 * 30;
    private final Map<String, CachedBlogTotal> blogTotalCache = new ConcurrentHashMap<>();

    public List<BlogReviewDto> searchReviews(String placeName) {
        String query = placeName + " 후기";
        return searchBlog(query, 3);
    }

    public List<BlogReviewDto> searchReviews(String placeName,
                                             String contextName,
                                             String contextAddress) {
        if (placeName == null || placeName.isBlank()) {
            return new ArrayList<>();
        }

        String safePlaceName = placeName.trim();
        String safeContextName = contextName == null ? "" : contextName.trim();
        String query = safeContextName.isBlank()
                ? safePlaceName + " 후기"
                : safeContextName + " " + safePlaceName + " 후기";

        List<BlogReviewDto> searched = searchBlog(query, 7);
        List<String> localityTerms = buildLocalityTerms(safeContextName, contextAddress);
        String normalizedPlaceName = normalizeSearchText(safePlaceName);
        List<BlogReviewDto> filtered = new ArrayList<>();

        for (BlogReviewDto review : searched) {
            String text = normalizeSearchText(
                    nullSafe(review.getTitle())
                            + " "
                            + nullSafe(review.getDescription())
                            + " "
                            + nullSafe(review.getBloggerName())
            );

            boolean hasPlaceSignal = normalizedPlaceName.isBlank()
                    || containsMeaningfulToken(text, normalizedPlaceName);
            boolean hasLocalSignal = localityTerms.isEmpty()
                    || containsAny(text, localityTerms);

            if (hasPlaceSignal && hasLocalSignal) {
                filtered.add(review);
            }

            if (filtered.size() >= 3) {
                break;
            }
        }

        return filtered;
    }

    public int countReviewResults(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            return 0;
        }

        String query = placeName.trim() + " 후기";
        String cacheKey = query.replaceAll("\\s+", " ");
        CachedBlogTotal cached = blogTotalCache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.getTotal();
        }

        int total = searchBlogTotal(query);
        blogTotalCache.put(cacheKey, new CachedBlogTotal(total));
        return total;
    }

    private List<BlogReviewDto> searchBlog(String query, int display) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        String url = "https://openapi.naver.com/v1/search/blog.json"
                + "?query=" + encodedQuery
                + "&display=" + display
                + "&start=1"
                + "&sort=sim";

        System.out.println("===== NAVER BLOG SEARCH QUERY =====");
        System.out.println(query);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                request,
                Map.class
        );

        Map responseBody = response.getBody();
        List<BlogReviewDto> result = new ArrayList<>();

        if (responseBody == null) {
            return result;
        }

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) responseBody.get("items");

        if (items == null || items.isEmpty()) {
            return result;
        }

        for (Map<String, Object> item : items) {
            String title = cleanHtml(asString(item.get("title")));
            String link = asString(item.get("link"));
            String description = cleanHtml(asString(item.get("description")));
            String bloggerName = cleanHtml(asString(item.get("bloggername")));
            String postDate = asString(item.get("postdate"));

            result.add(new BlogReviewDto(
                    title,
                    link,
                    description,
                    bloggerName,
                    postDate
            ));
        }

        return result;
    }

    private int searchBlogTotal(String query) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        String url = "https://openapi.naver.com/v1/search/blog.json"
                + "?query=" + encodedQuery
                + "&display=1"
                + "&start=1"
                + "&sort=sim";

        System.out.println("===== NAVER BLOG TOTAL QUERY =====");
        System.out.println(query);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map responseBody = response.getBody();

            if (responseBody == null) {
                return 0;
            }

            return asInt(responseBody.get("total"));
        } catch (Exception e) {
            System.out.println("네이버 블로그 후기 검색량 조회 실패: " + query);
            System.out.println(e.getMessage());
            return 0;
        }
    }

    private String cleanHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("<[^>]*>", "");
    }

    private List<String> buildLocalityTerms(String contextName, String contextAddress) {
        List<String> terms = new ArrayList<>();
        addLocalityTerm(terms, contextName);

        if (contextAddress != null && !contextAddress.isBlank()) {
            String[] parts = contextAddress.split("\\s+");

            for (String part : parts) {
                if (part.endsWith("시")
                        || part.endsWith("군")
                        || part.endsWith("구")
                        || part.endsWith("읍")
                        || part.endsWith("면")
                        || part.endsWith("동")
                        || part.endsWith("리")) {
                    addLocalityTerm(terms, part);
                }
            }
        }

        return terms;
    }

    private void addLocalityTerm(List<String> terms, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        String normalized = normalizeSearchText(value);

        if (normalized.endsWith("시")
                || normalized.endsWith("군")
                || normalized.endsWith("구")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.length() >= 2 && !terms.contains(normalized)) {
            terms.add(normalized);
        }
    }

    private boolean containsMeaningfulToken(String text, String normalizedPlaceName) {
        if (text.contains(normalizedPlaceName)) {
            return true;
        }

        for (String token : normalizedPlaceName.split("[^가-힣a-z0-9]+")) {
            if (token.length() >= 2 && text.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsAny(String text, List<String> terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }

        return cleanHtml(value)
                .replace("&amp;", "&")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String asString(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class CachedBlogTotal {

        private final int total;
        private final long cachedAt;

        CachedBlogTotal(int total) {
            this.total = total;
            this.cachedAt = System.currentTimeMillis();
        }

        int getTotal() {
            return total;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_TTL_MILLIS;
        }
    }
    
}
