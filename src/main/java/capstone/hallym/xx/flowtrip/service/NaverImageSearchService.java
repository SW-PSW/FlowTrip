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

import capstone.hallym.xx.flowtrip.dto.PlaceImageDto;

@Service
public class NaverImageSearchService {

    @Value("${naver.search.client-id}")
    private String clientId;

    @Value("${naver.search.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<PlaceImageDto> searchPlaceImages(String placeName) {
        String query = placeName + " 음식점";
        return searchImages(query, 3);
    }

    private List<PlaceImageDto> searchImages(String query, int display) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        String url = "https://openapi.naver.com/v1/search/image.json"
                + "?query=" + encodedQuery
                + "&display=" + display
                + "&start=1"
                + "&sort=sim";

        System.out.println("===== NAVER IMAGE SEARCH QUERY =====");
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

        List<PlaceImageDto> result = new ArrayList<>();

        if (response.getBody() == null) {
            return result;
        }

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) response.getBody().get("items");

        if (items == null || items.isEmpty()) {
            return result;
        }

        for (Map<String, Object> item : items) {
            String title = cleanHtml(asString(item.get("title")));
            String link = asString(item.get("link"));
            String thumbnail = asString(item.get("thumbnail"));

            result.add(new PlaceImageDto(title, link, thumbnail));
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