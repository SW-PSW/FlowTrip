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

    public List<NearbyPlaceDto> searchTargetPlace(String baseQuery) {
        return searchLocal(baseQuery, 1);
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

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) response.getBody().get("items");

        List<NearbyPlaceDto> result = new ArrayList<>();

        if (items == null) {
            return result;
        }

        for (Map<String, Object> item : items) {
            String title = cleanHtml((String) item.get("title"));
            String category = (String) item.get("category");
            String address = (String) item.get("address");
            String roadAddress = (String) item.get("roadAddress");
            String telephone = (String) item.get("telephone");
            String link = (String) item.get("link");
            String mapx = String.valueOf(item.get("mapx"));
            String mapy = String.valueOf(item.get("mapy"));

            result.add(new NearbyPlaceDto(
                    title,
                    category,
                    address,
                    roadAddress,
                    telephone,
                    link,
                    mapx,
                    mapy
            ));
        }

        return result;
    }

    private String cleanHtml(String value) {
        if (value == null) return "";
        return value.replaceAll("<[^>]*>", "");
    }
}