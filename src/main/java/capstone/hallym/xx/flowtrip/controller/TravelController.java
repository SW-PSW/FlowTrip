package capstone.hallym.xx.flowtrip.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.dto.NearbyPlaceWithReviewsDto;
import capstone.hallym.xx.flowtrip.dto.RecommendationCandidatesDto;
import capstone.hallym.xx.flowtrip.dto.RecommendationResultDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.service.NaverBlogSearchService;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;
import capstone.hallym.xx.flowtrip.service.OpenAiService;
import capstone.hallym.xx.flowtrip.service.RecommendationCandidateService;
import capstone.hallym.xx.flowtrip.service.ThemeService;
import jakarta.validation.Valid;

@Controller
public class TravelController {

    private final ThemeService themeService;
    private final RecommendationCandidateService recommendationCandidateService;
    private final OpenAiService openAiService;
    private final NaverLocalSearchService naverLocalSearchService;
    private final NaverBlogSearchService naverBlogSearchService;
    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    public TravelController(ThemeService themeService,
                            RecommendationCandidateService recommendationCandidateService,
                            OpenAiService openAiService,
                            NaverLocalSearchService naverLocalSearchService,
                            NaverBlogSearchService naverBlogSearchService,
                            PlaceRepository placeRepository) {

        this.themeService = themeService;
        this.recommendationCandidateService = recommendationCandidateService;
        this.openAiService = openAiService;
        this.naverLocalSearchService = naverLocalSearchService;
        this.naverBlogSearchService = naverBlogSearchService;
        this.placeRepository = placeRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/")
    public String showForm(Model model) {

        model.addAttribute("travelRequestDto", new TravelRequestDto());
        model.addAttribute("moodGroups", themeService.getMoodGroups());

        return "travel-form";
    }

    @PostMapping("/submit")
    public String submitForm(@Valid TravelRequestDto dto,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("moodGroups", themeService.getMoodGroups());
            return "travel-form";
        }

        RecommendationCandidatesDto candidates =
                recommendationCandidateService.findCandidates(dto);

        String gptResult = openAiService.requestRecommendation(
                dto,
                candidates.getThemeCandidatesText(),
                candidates.getPlaceCandidatesText()
        );

        System.out.println("===== GPT RESPONSE =====");
        System.out.println(gptResult);

        RecommendationResultDto recommendationResult =
                parseGptResult(gptResult);

        List<NearbyPlaceDto> targetPlaces = List.of();
        List<NearbyPlaceDto> restaurants = List.of();
        List<NearbyPlaceDto> cafes = List.of();
        List<NearbyPlaceDto> hotels = List.of();

        List<NearbyPlaceWithReviewsDto> restaurantReviews =
                new ArrayList<>();

        String naverSearchBaseQuery = "";

        if (recommendationResult != null
                && recommendationResult.getRecommendedPlaceId() != null
                && !recommendationResult.getRecommendedPlaceId().isBlank()) {

            try {

                Long placeId =
                        Long.parseLong(recommendationResult.getRecommendedPlaceId());

                Place selectedPlace =
                        placeRepository.findById(placeId).orElse(null);

                if (selectedPlace != null) {

                    String regionName = "";
                    String placeName = selectedPlace.getPlaceName();

                    if (selectedPlace.getRegion() != null) {
                        regionName =
                                selectedPlace.getRegion().getRegionName();
                    }

                    naverSearchBaseQuery =
                            naverLocalSearchService.buildSearchBaseQuery(
                                    regionName,
                                    placeName
                            );
                }

            } catch (Exception e) {

                System.out.println("추천 장소 ID 파싱 실패");
                System.out.println(e.getMessage());
            }
        }

        if (naverSearchBaseQuery.isBlank()
                && recommendationResult != null
                && recommendationResult.getRecommendedPlaceName() != null) {

            naverSearchBaseQuery =
                    naverLocalSearchService.buildSearchBaseQuery(
                            "",
                            recommendationResult.getRecommendedPlaceName()
                    );
        }

        if (!naverSearchBaseQuery.isBlank()) {

            System.out.println("네이버 검색 기준어 = "
                    + naverSearchBaseQuery);

            targetPlaces =
                    naverLocalSearchService.searchTargetPlace(
                            naverSearchBaseQuery
                    );

            restaurants =
                    naverLocalSearchService.searchRestaurantsNear(
                            naverSearchBaseQuery
                    );

            cafes =
                    naverLocalSearchService.searchCafesNear(
                            naverSearchBaseQuery
                    );

            hotels =
                    naverLocalSearchService.searchHotelsNear(
                            naverSearchBaseQuery
                    );

            System.out.println("추천 장소 검색 결과 개수 = "
                    + targetPlaces.size());

            System.out.println("식당 검색 결과 개수 = "
                    + restaurants.size());

            System.out.println("카페 검색 결과 개수 = "
                    + cafes.size());

            System.out.println("숙소 검색 결과 개수 = "
                    + hotels.size());

            if (!targetPlaces.isEmpty()) {

                NearbyPlaceDto target = targetPlaces.get(0);

                System.out.println(
                        "=== 최종 추천 장소 네이버 검색 결과 ===");

                System.out.println("title = "
                        + target.getTitle());

                System.out.println("roadAddress = "
                        + target.getRoadAddress());

                System.out.println("address = "
                        + target.getAddress());

                System.out.println("mapx = "
                        + target.getMapx());

                System.out.println("mapy = "
                        + target.getMapy());
            }

            for (NearbyPlaceDto restaurant : restaurants) {

                System.out.println(
                        "===== 블로그 후기 검색 시작 =====");

                System.out.println("업체명 = "
                        + restaurant.getTitle());

                restaurantReviews.add(

                        new NearbyPlaceWithReviewsDto(

                                restaurant,

                                naverBlogSearchService.searchReviews(
                                        restaurant.getTitle()
                                )
                        )
                );
            }
        }

        model.addAttribute("request", dto);

        model.addAttribute("gptResult", gptResult);

        model.addAttribute(
                "recommendationResult",
                recommendationResult
        );

        model.addAttribute(
                "themeCandidates",
                candidates.getThemeCandidatesText()
        );

        model.addAttribute(
                "placeCandidates",
                candidates.getPlaceCandidatesText()
        );

        model.addAttribute("targetPlaces", targetPlaces);

        model.addAttribute("restaurants", restaurants);

        model.addAttribute("cafes", cafes);

        model.addAttribute("hotels", hotels);

        model.addAttribute(
                "restaurantReviews",
                restaurantReviews
        );

        model.addAttribute(
                "naverSearchBaseQuery",
                naverSearchBaseQuery
        );

        model.addAttribute(
                "naverMapClientId",
                naverMapClientId
        );

        return "travel-result";
    }

    private RecommendationResultDto parseGptResult(String gptResult) {

        try {

            String cleanedJson = gptResult
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(
                    cleanedJson,
                    RecommendationResultDto.class
            );

        } catch (Exception e) {

            System.out.println("GPT JSON 파싱 실패");
            System.out.println(e.getMessage());

            return null;
        }
    }
}