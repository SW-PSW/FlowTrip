package capstone.hallym.xx.flowtrip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<BlogReviewDto> searchReviews(String placeName) {
        String query = placeName + " 후기";
        return searchBlog(query, 3);
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

    private String cleanHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("<[^>]*>", "");
    }

    private String asString(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }
}