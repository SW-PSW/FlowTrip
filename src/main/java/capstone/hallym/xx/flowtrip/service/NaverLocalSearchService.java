package capstone.hallym.xx.flowtrip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
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

    private static final int NAVER_LOCAL_MAX_DISPLAY = 5;
    private static final int NAVER_LOCAL_MAX_PAGE = 2;

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

    public List<NearbyPlaceDto> searchPlacesByKeyword(String query) {
        return searchLocal(query, 10);
    }

    public void applyDistanceAndSort(NearbyPlaceDto target, List<NearbyPlaceDto> places) {
        if (target == null || places == null || places.isEmpty()) {
            return;
        }

        applyDistanceAndSortByMapxy(target.getMapx(), target.getMapy(), places);
    }

    public void applyDistanceAndSortByMapxy(String targetMapx,
                                            String targetMapy,
                                            List<NearbyPlaceDto> places) {
        if (isBlank(targetMapx) || isBlank(targetMapy) || places == null) {
            return;
        }

        double targetLng = parseCoord(targetMapx);
        double targetLat = parseCoord(targetMapy);

        for (NearbyPlaceDto place : places) {
            if (isBlank(place.getMapx()) || isBlank(place.getMapy())) {
                place.setDistanceKm(9999);
                continue;
            }

            double placeLng = parseCoord(place.getMapx());
            double placeLat = parseCoord(place.getMapy());

            double distance = calculateDistanceKm(
                    targetLat,
                    targetLng,
                    placeLat,
                    placeLng
            );

            place.setDistanceKm(distance);
        }

        places.sort(Comparator.comparingDouble(NearbyPlaceDto::getDistanceKm));
    }

    private List<NearbyPlaceDto> searchLocal(String query, int totalDisplay) {
        List<NearbyPlaceDto> result = new ArrayList<>();

        int start = 1;
        int pageCount = 0;

        while (result.size() < totalDisplay && pageCount < NAVER_LOCAL_MAX_PAGE) {
            int remain = totalDisplay - result.size();
            int display = Math.min(NAVER_LOCAL_MAX_DISPLAY, remain);

            List<NearbyPlaceDto> pageResult = searchLocalPage(query, display, start);

            if (pageResult.isEmpty()) {
                break;
            }

            for (NearbyPlaceDto place : pageResult) {
                if (!containsSamePlace(result, place)) {
                    result.add(place);
                }

                if (result.size() >= totalDisplay) {
                    break;
                }
            }

            start += display;
            pageCount++;

            if (pageResult.size() < display) {
                break;
            }
        }

        return result;
    }

    private List<NearbyPlaceDto> searchLocalPage(String query, int display, int start) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        String url = "https://openapi.naver.com/v1/search/local.json"
                + "?query=" + encodedQuery
                + "&display=" + display
                + "&start=" + start
                + "&sort=sim";

        System.out.println("===== NAVER LOCAL SEARCH QUERY =====");
        System.out.println(query + " / start=" + start + " / display=" + display);

        List<NearbyPlaceDto> result = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response;

        try {
            response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    request,
                    Map.class
            );
        } catch (Exception e) {
            System.out.println("네이버 지역 검색 실패: " + query);
            System.out.println(e.getMessage());
            return result;
        }

        Map responseBody = response.getBody();

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
            String category = asString(item.get("category"));
            String address = asString(item.get("address"));
            String roadAddress = asString(item.get("roadAddress"));
            String telephone = asString(item.get("telephone"));
            String link = asString(item.get("link"));
            String mapx = asString(item.get("mapx"));
            String mapy = asString(item.get("mapy"));

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

    private boolean containsSamePlace(List<NearbyPlaceDto> places, NearbyPlaceDto target) {
        for (NearbyPlaceDto place : places) {
            boolean sameTitle = safeEquals(place.getTitle(), target.getTitle());

            boolean sameAddress =
                    safeEquals(place.getAddress(), target.getAddress())
                            || safeEquals(place.getRoadAddress(), target.getRoadAddress());

            boolean sameMap =
                    safeEquals(place.getMapx(), target.getMapx())
                            && safeEquals(place.getMapy(), target.getMapy());

            if ((sameTitle && sameAddress) || sameMap) {
                return true;
            }
        }

        return false;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        return a.trim().equals(b.trim());
    }

    private double parseCoord(String value) {
        return Double.parseDouble(value) / 10000000;
    }

    private double calculateDistanceKm(double lat1,
                                       double lon1,
                                       double lat2,
                                       double lon2) {
        final double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}