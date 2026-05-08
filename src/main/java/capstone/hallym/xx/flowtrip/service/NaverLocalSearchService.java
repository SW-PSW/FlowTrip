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

import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;

@Service
public class NaverLocalSearchService {

    @Value("${naver.search.client-id}")
    private String clientId;

    @Value("${naver.search.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String buildSearchBaseQuery(String regionName, String placeName) {
        String safeRegionName = nullSafe(regionName).trim();
        String safePlaceName = nullSafe(placeName).trim();

        if (safeRegionName.isBlank()) {
            return safePlaceName;
        }

        return "강원특별자치도 " + safeRegionName + " " + safePlaceName;
    }

    public List<NearbyPlaceDto> searchTargetPlace(String baseQuery) {
        return searchLocal(baseQuery, 5);
    }

    public List<NearbyPlaceDto> searchRestaurantsNear(String baseQuery) {
        return searchLocal(baseQuery + " 근처 식당", 10);
    }

    public List<NearbyPlaceDto> searchCafesNear(String baseQuery) {
        return searchLocal(baseQuery + " 근처 카페", 10);
    }

    public List<NearbyPlaceDto> searchHotelsNear(String baseQuery) {
        return searchLocal(baseQuery + " 근처 숙소", 10);
    }

    private List<NearbyPlaceDto> searchLocal(String query, int display) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        String url = "https://openapi.naver.com/v1/search/local.json"
                + "?query=" + encodedQuery
                + "&display=" + display
                + "&start=1"
                + "&sort=sim";

        System.out.println("===== NAVER LOCAL SEARCH QUERY =====");
        System.out.println(query);
        System.out.println("===== NAVER LOCAL SEARCH URL =====");
        System.out.println(url);

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

        List<NearbyPlaceDto> result = new ArrayList<>();

        if (responseBody == null) {
            System.out.println("네이버 지역 검색 응답이 비어 있습니다.");
            return result;
        }

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) responseBody.get("items");

        if (items == null || items.isEmpty()) {
            System.out.println("네이버 지역 검색 결과 없음: " + query);
            return result;
        }

        for (Map<String, Object> item : items) {
            String title = cleanHtml(asString(item.get("title")));
            String category = asString(item.get("category"));
            String address = asString(item.get("address"));
            String roadAddress = asString(item.get("roadAddress"));
            String telephone = asString(item.get("telephone"));
            String link = asString(item.get("link"));
            String mapx = asString(item.get("mapx"));
            String mapy = asString(item.get("mapy"));

            NearbyPlaceDto dto = new NearbyPlaceDto(
                    title,
                    category,
                    address,
                    roadAddress,
                    telephone,
                    link,
                    mapx,
                    mapy
            );

            result.add(dto);

            System.out.println("----- NAVER RESULT ITEM -----");
            System.out.println("title = " + title);
            System.out.println("category = " + category);
            System.out.println("address = " + address);
            System.out.println("roadAddress = " + roadAddress);
            System.out.println("mapx = " + mapx);
            System.out.println("mapy = " + mapy);
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

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}