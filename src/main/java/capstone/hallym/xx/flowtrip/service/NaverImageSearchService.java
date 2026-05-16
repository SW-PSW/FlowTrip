package capstone.hallym.xx.flowtrip.service;

import java.net.URI;
import java.net.URLEncoder;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public String searchThumbnail(String placeName, String category, String address) {

        try {
            String cleanPlaceName = cleanText(placeName);
            String cleanCategory = cleanText(category);

            String query;

            if (cleanCategory.contains("카페")) {
                query = cleanPlaceName + " 카페 후기";
            } else if (cleanCategory.contains("숙박")
                    || cleanCategory.contains("펜션")
                    || cleanCategory.contains("호텔")
                    || cleanCategory.contains("모텔")) {
                query = cleanPlaceName + " 숙소 후기";
            } else if (cleanCategory.contains("관광")
                    || cleanCategory.contains("문화")
                    || cleanCategory.contains("박물관")
                    || cleanCategory.contains("전시")) {
                query = cleanPlaceName + " 방문 후기";
            } else {
                query = cleanPlaceName + " 맛집 후기";
            }

            List<PlaceImageDto> images = searchImages(query, 5);

            if (images == null || images.isEmpty()) {
                return null;
            }

            for (PlaceImageDto image : images) {
                String thumbnail = image.getThumbnail();

                if (thumbnail != null
                        && !thumbnail.isBlank()
                        && (thumbnail.contains(".jpg")
                        || thumbnail.contains(".png")
                        || thumbnail.contains(".jpeg")
                        || thumbnail.contains("pstatic"))) {

                    return thumbnail;
                }
            }

            return null;

        } catch (Exception e) {
            System.out.println("네이버 이미지 검색 실패 - 썸네일 없음 처리");
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]*>", "")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isBadImage(String text) {
        String[] badWords = {
                "코로나", "covid", "독감", "예방접종", "지원금", "어르신",
                "카드뉴스", "포스터", "배너", "공지", "안내문", "홍보",
                "로고", "지도", "약도", "표", "테이블", "메뉴판"
        };

        for (String badWord : badWords) {
            if (text.contains(badWord)) {
                return true;
            }
        }

        return false;
    }
}