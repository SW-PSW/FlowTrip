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

import capstone.hallym.xx.flowtrip.dto.RecommendationCandidatesDto;
import capstone.hallym.xx.flowtrip.dto.RecommendationResultDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.service.NaverImageSearchService;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;
import capstone.hallym.xx.flowtrip.service.OpenAiService;
import capstone.hallym.xx.flowtrip.service.RecommendationCandidateService;
import capstone.hallym.xx.flowtrip.service.ThemeService;
import capstone.hallym.xx.flowtrip.dto.CongestionAnalysisDto;
import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.service.CongestionAnalysisService;
import jakarta.validation.Valid;

@Controller
public class TravelController {

    private final ThemeService themeService;
    private final RecommendationCandidateService recommendationCandidateService;
    private final OpenAiService openAiService;
    private final NaverLocalSearchService naverLocalSearchService;
    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;
    private final NaverImageSearchService naverImageSearchService;
    private final CongestionAnalysisService congestionAnalysisService;
    
    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    public TravelController(ThemeService themeService,
                            RecommendationCandidateService recommendationCandidateService,
                            OpenAiService openAiService,
                            NaverLocalSearchService naverLocalSearchService,
                            PlaceRepository placeRepository,
                            NaverImageSearchService naverImageSearchService, CongestionAnalysisService congestionAnalysisService) {

        this.themeService = themeService;
        this.recommendationCandidateService = recommendationCandidateService;
        this.openAiService = openAiService;
        this.naverLocalSearchService = naverLocalSearchService;
        this.placeRepository = placeRepository;
        this.objectMapper = new ObjectMapper();
        this.naverImageSearchService = naverImageSearchService;
        this.congestionAnalysisService = congestionAnalysisService;
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
		
		RecommendationResultDto recommendationResult = parseGptResult(gptResult);
		
		List<NearbyPlaceDto> targetPlaces = new ArrayList<>();
		List<NearbyPlaceDto> restaurants = new ArrayList<>();
		List<NearbyPlaceDto> cafes = new ArrayList<>();
		List<NearbyPlaceDto> hotels = new ArrayList<>();
		List<NearbyPlaceDto> attractions = new ArrayList<>();
		
		String naverSearchBaseQuery = "";
		
		/*
		* 핵심:
		* 추천 장소 엔티티를 가져와서
		* 혼잡도 분석에도 사용
		*/
		Place selectedPlace = null;
		
		if (recommendationResult != null
		&& recommendationResult.getRecommendedPlaceId() != null
		&& !recommendationResult.getRecommendedPlaceId().isBlank()) {
		
		try {
		
		Long placeId =
		       Long.parseLong(recommendationResult.getRecommendedPlaceId());
		
		selectedPlace =
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
		
		/*
		* fallback:
		* placeId가 없어도 장소명으로 검색
		*/
		if (naverSearchBaseQuery.isBlank()
		&& recommendationResult != null
		&& recommendationResult.getRecommendedPlaceName() != null
		&& !recommendationResult.getRecommendedPlaceName().isBlank()) {
		
		naverSearchBaseQuery =
		   naverLocalSearchService.buildSearchBaseQuery(
		           "",
		           recommendationResult.getRecommendedPlaceName()
		   );
		}
		
		/*
		* ===========================
		* FlowTrip 핵심 기능
		* 혼잡도 서버 분석 추가
		* ===========================
		*/
		CongestionAnalysisDto congestionAnalysis = null;
		
		if (recommendationResult != null) {
		
		congestionAnalysis =
		   congestionAnalysisService.analyze(
		           dto,
		           recommendationResult,
		           selectedPlace
		   );
		
		/*
		* AI 응답 DTO에 서버 계산 혼잡도 덮어쓰기
		*/
		recommendationResult.applyCongestionAnalysis(
		   congestionAnalysis
		);
		
		System.out.println("===== 혼잡도 분석 결과 =====");
		System.out.println("점수 = " + congestionAnalysis.getCongestionScore());
		System.out.println("등급 = " + congestionAnalysis.getCongestionLevel());
		System.out.println("사유 = " + congestionAnalysis.getCongestionReason());
		}
		
		/*
		* 네이버 검색
		*/
		if (!naverSearchBaseQuery.isBlank()) {
		
		System.out.println("네이버 검색 기준어 = " + naverSearchBaseQuery);
		
		/*
		* 대표 장소
		*/
		targetPlaces =
		   naverLocalSearchService.searchTargetPlace(
		           naverSearchBaseQuery
		   );
		
		/*
		* 식당
		*/
		restaurants =
		   searchByKeywordsOrDefault(
		           recommendationResult == null
		                   ? null
		                   : recommendationResult.getRestaurantKeywords(),
		           naverSearchBaseQuery + " 근처 식당",
		           10
		   );
		
		/*
		* 카페
		*/
		cafes =
		   searchByKeywordsOrDefault(
		           recommendationResult == null
		                   ? null
		                   : recommendationResult.getCafeKeywords(),
		           naverSearchBaseQuery + " 근처 카페",
		           10
		   );
		
		/*
		* 숙소
		*/
		hotels =
		   searchByKeywordsOrDefault(
		           recommendationResult == null
		                   ? null
		                   : recommendationResult.getHotelKeywords(),
		           naverSearchBaseQuery + " 근처 숙소",
		           10
		   );
		
		/*
		* 관광지
		*/
		attractions =
		   searchByKeywordsOrDefault(
		           recommendationResult == null
		                   ? null
		                   : recommendationResult.getAttractionKeywords(),
		           naverSearchBaseQuery + " 근처 관광지",
		           10
		   );
		
			/*
			* 거리 계산
			*/
			if (!targetPlaces.isEmpty()) {
			
			NearbyPlaceDto target = targetPlaces.get(0);
			
			naverLocalSearchService.applyDistanceAndSort(
			       target,
			       restaurants
			);
			
			naverLocalSearchService.applyDistanceAndSort(
			       target,
			       cafes
			);
			
			naverLocalSearchService.applyDistanceAndSort(
			       target,
			       hotels
			);
			
			naverLocalSearchService.applyDistanceAndSort(
			       target,
			       attractions
			);
			
			System.out.println("=== 최종 추천 장소 네이버 검색 결과 ===");
			System.out.println("title = " + target.getTitle());
			System.out.println("roadAddress = " + target.getRoadAddress());
			System.out.println("address = " + target.getAddress());
			System.out.println("mapx = " + target.getMapx());
			System.out.println("mapy = " + target.getMapy());
			}
			
			System.out.println("추천 장소 검색 결과 개수 = " + targetPlaces.size());
			System.out.println("식당 검색 결과 개수 = " + restaurants.size());
			System.out.println("카페 검색 결과 개수 = " + cafes.size());
			System.out.println("숙소 검색 결과 개수 = " + hotels.size());
			System.out.println("관광지 검색 결과 개수 = " + attractions.size());
			}
			
			/*
			* 이미지 채우기
			*/
			fillImages(limitList(restaurants, 3));
			fillImages(limitList(cafes, 3));
			fillImages(limitList(hotels, 2));
			fillImages(limitList(attractions, 2));
			
			/*
			* model 전달
			*/
			model.addAttribute("request", dto);
			
			model.addAttribute("gptResult", gptResult);
			
			model.addAttribute(
			"recommendationResult",
			recommendationResult
			);
			
			model.addAttribute(
			"congestionAnalysis",
			congestionAnalysis
			);
			fillImages(restaurants);
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
			model.addAttribute("attractions", attractions);
			
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

    private List<NearbyPlaceDto> searchByKeywordsOrDefault(List<String> keywords,
                                                           String defaultQuery,
                                                           int limit) {

        List<NearbyPlaceDto> result = new ArrayList<>();

        if (keywords != null && !keywords.isEmpty()) {
            for (String keyword : keywords) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }

                List<NearbyPlaceDto> searched =
                        naverLocalSearchService.searchPlacesByKeyword(keyword);

                addUniquePlaces(result, searched, limit);

                if (result.size() >= limit) {
                    break;
                }
            }
        }

        if (result.isEmpty()) {
            result = naverLocalSearchService.searchPlacesByKeyword(defaultQuery);
        }

        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }

        return result;
    }

    private void addUniquePlaces(List<NearbyPlaceDto> targetList,
                                 List<NearbyPlaceDto> sourceList,
                                 int limit) {

        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }

        for (NearbyPlaceDto place : sourceList) {
            if (!containsSamePlace(targetList, place)) {
                targetList.add(place);
            }

            if (targetList.size() >= limit) {
                break;
            }
        }
    }

    private boolean containsSamePlace(List<NearbyPlaceDto> places,
                                      NearbyPlaceDto target) {

        if (target == null) {
            return true;
        }

        for (NearbyPlaceDto place : places) {
            boolean sameTitle =
                    safeEquals(place.getTitle(), target.getTitle());

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

    private void fillImages(List<NearbyPlaceDto> places) {

        if (places == null || places.isEmpty()) {
            return;
        }

        int maxImageSearchCount = Math.min(places.size(), 3);

        for (int i = 0; i < maxImageSearchCount; i++) {

            NearbyPlaceDto place = places.get(i);

            try {
                String cleanTitle = place.getTitle()
                        .replaceAll("<[^>]*>", "")
                        .replace("&amp;", "&");

                String address = place.getRoadAddress();

                if (address == null || address.isBlank()) {
                    address = place.getAddress();
                }

                String imageUrl =
                        naverImageSearchService.searchThumbnail(
                                cleanTitle,
                                place.getCategory(),
                                address
                        );

                System.out.println("대표 이미지: " + cleanTitle + " -> " + imageUrl);

                place.setImageUrl(imageUrl);

            } catch (Exception e) {
                System.out.println("이미지 검색 실패 - 기본 이미지로 대체");
                System.out.println(e.getMessage());
                place.setImageUrl(null);
            }
        }
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

    private boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        return a.trim().equals(b.trim());
    }
    private List<NearbyPlaceDto> limitList(List<NearbyPlaceDto> list, int limit) {

        if (list == null || list.isEmpty()) {
            return list;
        }

        return list.stream()
                .limit(limit)
                .toList();
    }
}