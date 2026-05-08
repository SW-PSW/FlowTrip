package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;

@RestController
public class NaverTestController {

    private final NaverLocalSearchService naverLocalSearchService;

    public NaverTestController(NaverLocalSearchService naverLocalSearchService) {
        this.naverLocalSearchService = naverLocalSearchService;
    }

    @GetMapping("/api/test/restaurants")
    public List<NearbyPlaceDto> testRestaurants() {
        return naverLocalSearchService.searchRestaurantsNear("정동심곡 바다부채길");
    }
}