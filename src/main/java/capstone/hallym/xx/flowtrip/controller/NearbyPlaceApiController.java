package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;

@RestController
public class NearbyPlaceApiController {

    private final NaverLocalSearchService naverLocalSearchService;
    private final TravelCourseItemRepository travelCourseItemRepository;

    public NearbyPlaceApiController(NaverLocalSearchService naverLocalSearchService,
                                    TravelCourseItemRepository travelCourseItemRepository) {
        this.naverLocalSearchService = naverLocalSearchService;
        this.travelCourseItemRepository = travelCourseItemRepository;
    }

    @GetMapping("/api/places/search")
    public List<NearbyPlaceDto> searchPlaces(@RequestParam String query,
                                             @RequestParam String targetMapx,
                                             @RequestParam String targetMapy) {

        List<NearbyPlaceDto> places =
                naverLocalSearchService.searchPlacesByKeyword(query);

        naverLocalSearchService.applyDistanceAndSortByMapxy(
                targetMapx,
                targetMapy,
                places
        );

        applySavedCounts(places);

        return places;
    }

    private void applySavedCounts(List<NearbyPlaceDto> places) {
        if (places == null || places.isEmpty()) {
            return;
        }

        for (NearbyPlaceDto place : places) {
            String placeName = cleanPlaceTitle(place.getTitle());

            if (placeName.isBlank()) {
                place.setSavedCount(0);
            } else {
                place.setSavedCount(countSavedPlace(placeName));
            }
        }
    }

    private long countSavedPlace(String placeName) {
        long count = travelCourseItemRepository.countByPlaceName(placeName);

        if (count == 0) {
            count = travelCourseItemRepository.countByPlaceNameContaining(placeName);
        }

        return count;
    }

    private String cleanPlaceTitle(String title) {
        if (title == null) {
            return "";
        }

        return title
                .replaceAll("<[^>]*>", "")
                .replace("&amp;", "&")
                .trim();
    }
}
