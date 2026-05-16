package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;

@RestController
public class NearbyPlaceApiController {

    private final NaverLocalSearchService naverLocalSearchService;

    public NearbyPlaceApiController(NaverLocalSearchService naverLocalSearchService) {
        this.naverLocalSearchService = naverLocalSearchService;
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

        return places;
    }
}