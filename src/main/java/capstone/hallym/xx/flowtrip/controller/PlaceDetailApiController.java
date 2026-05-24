package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import capstone.hallym.xx.flowtrip.dto.BlogReviewDto;
import capstone.hallym.xx.flowtrip.dto.PlaceImageDto;
import capstone.hallym.xx.flowtrip.dto.PlaceReviewResponseDto;
import capstone.hallym.xx.flowtrip.service.NaverBlogSearchService;
import capstone.hallym.xx.flowtrip.service.NaverImageSearchService;

@RestController
public class PlaceDetailApiController {

    private final NaverBlogSearchService naverBlogSearchService;
    private final NaverImageSearchService naverImageSearchService;

    public PlaceDetailApiController(
            NaverBlogSearchService naverBlogSearchService,
            NaverImageSearchService naverImageSearchService
    ) {
        this.naverBlogSearchService = naverBlogSearchService;
        this.naverImageSearchService = naverImageSearchService;
    }

    @GetMapping("/api/place/detail")
    public PlaceReviewResponseDto getPlaceDetail(
            @RequestParam String placeName,
            @RequestParam(required = false) String contextName,
            @RequestParam(required = false) String contextAddress
    ) {

        System.out.println("===== PLACE DETAIL API =====");
        System.out.println("placeName = " + placeName);
        System.out.println("contextName = " + contextName);
        System.out.println("contextAddress = " + contextAddress);

        List<BlogReviewDto> reviews =
                naverBlogSearchService.searchReviews(placeName, contextName, contextAddress);

        List<PlaceImageDto> images =
                naverImageSearchService.searchPlaceImages(placeName);

        return new PlaceReviewResponseDto(
                placeName,
                reviews,
                images
        );
    }
}
