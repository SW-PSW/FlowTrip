package capstone.hallym.xx.flowtrip.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import capstone.hallym.xx.flowtrip.dto.RecommendationCandidatesDto;
import capstone.hallym.xx.flowtrip.dto.RecommendationResultDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.dto.WeatherForecastDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.repository.UserRepository;
import capstone.hallym.xx.flowtrip.service.NaverBlogSearchService;
import capstone.hallym.xx.flowtrip.service.NaverImageSearchService;
import capstone.hallym.xx.flowtrip.service.NaverLocalSearchService;
import capstone.hallym.xx.flowtrip.service.OpenAiService;
import capstone.hallym.xx.flowtrip.service.RecommendationCandidateService;
import capstone.hallym.xx.flowtrip.service.ThemeService;
import capstone.hallym.xx.flowtrip.service.WeatherForecastService;
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
    private final WeatherForecastService weatherForecastService;
    private final TravelCourseItemRepository travelCourseItemRepository;
    private final UserRepository userRepository;
    private final NaverBlogSearchService naverBlogSearchService;
    
    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    public TravelController(ThemeService themeService,
                            RecommendationCandidateService recommendationCandidateService,
                            OpenAiService openAiService,
                            NaverLocalSearchService naverLocalSearchService,
                            PlaceRepository placeRepository,
                            NaverImageSearchService naverImageSearchService,
                            CongestionAnalysisService congestionAnalysisService,
                            WeatherForecastService weatherForecastService,
                            TravelCourseItemRepository travelCourseItemRepository,
                            UserRepository userRepository,
                            NaverBlogSearchService naverBlogSearchService) {

        this.themeService = themeService;
        this.recommendationCandidateService = recommendationCandidateService;
        this.openAiService = openAiService;
        this.naverLocalSearchService = naverLocalSearchService;
        this.placeRepository = placeRepository;
        this.objectMapper = new ObjectMapper();
        this.naverImageSearchService = naverImageSearchService;
        this.congestionAnalysisService = congestionAnalysisService;
        this.weatherForecastService = weatherForecastService;
        this.travelCourseItemRepository = travelCourseItemRepository;
        this.userRepository = userRepository;
        this.naverBlogSearchService = naverBlogSearchService;
    }
    
    @GetMapping("/api/mood-groups")
    @ResponseBody
    public List<String> getMoodGroups() {
        return themeService.getMoodGroups();
    }

    @GetMapping("/")
    public String showForm() {
        return "redirect:/react/index.html";
    }

    @GetMapping("/travel-result/latest")
    public String showLatestTravelResult(HttpSession session,
                                         Model model,
                                         Authentication authentication) {

        Boolean hasLatestTravelResult =
                (Boolean) session.getAttribute("lastTravelResultExists");

        if (!Boolean.TRUE.equals(hasLatestTravelResult)) {
            return "redirect:/react/index.html";
        }

        restoreLastTravelResult(session, model);
        model.addAttribute("currentUserDisplayName", resolveCurrentUserDisplayName(authentication));
        model.addAttribute("autoOpenAiModal", false);
        return "travel-result";
    }

    @PostMapping("/submit")
    public String submitForm(@Valid TravelRequestDto dto,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            Authentication authentication) {

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
		String selectedRegionName = "";
		String selectedPlaceName = "";
		
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
		   selectedPlaceName = placeName == null ? "" : placeName;
		
		   if (selectedPlace.getRegion() != null) {
		       regionName =
		               selectedPlace.getRegion().getRegionName();
		       selectedRegionName = regionName == null ? "" : regionName;
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

		selectedPlaceName = recommendationResult.getRecommendedPlaceName();
		
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
		WeatherForecastDto weatherForecast = null;
		List<WeatherForecastDto> weatherForecasts = new ArrayList<>();
		
		/*
		* 네이버 검색
		*/
		if (!naverSearchBaseQuery.isBlank()) {
		
		System.out.println("네이버 검색 기준어 = " + naverSearchBaseQuery);
		
		/*
		* 대표 장소
		*/
		targetPlaces =
		   resolveTargetPlaces(
		           naverSearchBaseQuery,
		           selectedRegionName,
		           selectedPlaceName
		   );
		if (targetPlaces == null || targetPlaces.isEmpty()) {
		    System.out.println("대표 장소 검색 실패 → 추천 장소명으로 재검색");

		    if (recommendationResult != null
		            && recommendationResult.getRecommendedPlaceName() != null
		            && !recommendationResult.getRecommendedPlaceName().isBlank()) {

		        targetPlaces =
		                resolveTargetPlaces(
		                        recommendationResult.getRecommendedPlaceName(),
		                        selectedRegionName,
		                        selectedPlaceName
		                );
		    }
		}
		
		/*
		* 식당
		*/
		String nearbySearchBaseQuery =
		        buildNearbySearchBaseQuery(targetPlaces, selectedRegionName, selectedPlaceName);

		restaurants =
		   searchNearbyPlaces(
		           nearbySearchBaseQuery,
		           selectedRegionName,
		           "식당",
		           10
		   );
		
		/*
		* 카페
		*/
		cafes =
		   searchNearbyPlaces(
		           nearbySearchBaseQuery,
		           selectedRegionName,
		           "카페",
		           10
		   );
		
		/*
		* 숙소
		*/
		hotels =
		   searchNearbyPlaces(
		           nearbySearchBaseQuery,
		           selectedRegionName,
		           "숙소",
		           10
		   );
		
		/*
		* 관광지
		*/
		attractions =
		   searchNearbyPlaces(
		           nearbySearchBaseQuery,
		           selectedRegionName,
		           "관광지",
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

			keepNearbyPlaces(restaurants, 12.0, 3);
			keepNearbyPlaces(cafes, 12.0, 3);
			keepNearbyPlaces(hotels, 25.0, 3);
			keepNearbyPlaces(attractions, 15.0, 3);
			keepRegionPlaces(restaurants, selectedRegionName, 3);
			keepRegionPlaces(cafes, selectedRegionName, 3);
			keepRegionPlaces(hotels, selectedRegionName, 3);
			keepRegionPlaces(attractions, selectedRegionName, 3);
			
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

			if (recommendationResult != null) {

			Double weatherLatitude = resolveWeatherLatitude(selectedPlace, targetPlaces);
			Double weatherLongitude = resolveWeatherLongitude(selectedPlace, targetPlaces);

			weatherForecasts =
			   weatherForecastService.getForecasts(
			           weatherLatitude,
			           weatherLongitude,
			           dto.getStartDate(),
			           dto.getEndDate()
			   );

			if (!weatherForecasts.isEmpty()) {
			    weatherForecast = weatherForecasts.get(0);
			} else {
			    weatherForecast =
			       weatherForecastService.getForecast(
			               weatherLatitude,
			               weatherLongitude,
			               dto.getStartDate()
			       );
			}

            NearbyPlaceDto targetPlace = targetPlaces == null || targetPlaces.isEmpty()
                    ? null
                    : targetPlaces.get(0);
            String targetPlaceName = targetPlace == null
                    ? selectedPlaceName
                    : cleanPlaceTitle(targetPlace.getTitle());
            int naverReviewResultCount = naverBlogSearchService.countReviewResults(targetPlaceName);
            long targetSavedCount = countSavedPlace(targetPlaceName);
			
			congestionAnalysis =
			   congestionAnalysisService.analyze(
			           dto,
			           recommendationResult,
			           selectedPlace,
			           weatherForecast,
                       targetPlace,
                       naverReviewResultCount,
                       targetSavedCount
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
			* 이미지 채우기
			*/
			fillImages(limitList(restaurants, 3));
			fillImages(limitList(cafes, 3));
			fillImages(limitList(hotels, 2));
			fillImages(limitList(attractions, 2));

			applySavedCounts(targetPlaces);
			applySavedCounts(restaurants);
			applySavedCounts(cafes);
			applySavedCounts(hotels);
			applySavedCounts(attractions);
			
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
			model.addAttribute(
			"weatherForecast",
			weatherForecast
			);
			model.addAttribute(
			"weatherForecasts",
			weatherForecasts
			);
			model.addAttribute(
			"currentUserDisplayName",
			resolveCurrentUserDisplayName(authentication)
			);
			model.addAttribute("autoOpenAiModal", true);
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
			
			model.addAttribute(
			        "targetPlaceData",
			        !targetPlaces.isEmpty() ? targetPlaces.get(0) : null
			);
			
			model.addAttribute("restaurants", restaurants);
			model.addAttribute("cafes", cafes);
			model.addAttribute("hotels", hotels);
			model.addAttribute("attractions", attractions);
			
			model.addAttribute(
			"naverSearchBaseQuery",
			naverSearchBaseQuery
			);
			model.addAttribute("nearbySearchBaseQuery", buildNearbySearchBaseQuery(targetPlaces, selectedRegionName, selectedPlaceName));
			model.addAttribute("selectedRegionName", selectedRegionName);
			
			model.addAttribute(
			"naverMapClientId",
			naverMapClientId
			);

			storeLastTravelResult(
			        session,
			        dto,
			        gptResult,
			        recommendationResult,
			        congestionAnalysis,
			        weatherForecast,
			        weatherForecasts,
			        candidates,
			        targetPlaces,
			        restaurants,
			        cafes,
			        hotels,
			        attractions,
			        naverSearchBaseQuery,
			        buildNearbySearchBaseQuery(targetPlaces, selectedRegionName, selectedPlaceName),
			        selectedRegionName
			);
			
			return "travel-result";
			}

    private void storeLastTravelResult(HttpSession session,
                                       TravelRequestDto request,
                                       String gptResult,
                                       RecommendationResultDto recommendationResult,
                                       CongestionAnalysisDto congestionAnalysis,
                                       WeatherForecastDto weatherForecast,
                                       List<WeatherForecastDto> weatherForecasts,
                                       RecommendationCandidatesDto candidates,
                                       List<NearbyPlaceDto> targetPlaces,
                                       List<NearbyPlaceDto> restaurants,
                                       List<NearbyPlaceDto> cafes,
                                       List<NearbyPlaceDto> hotels,
                                       List<NearbyPlaceDto> attractions,
                                       String naverSearchBaseQuery,
                                       String nearbySearchBaseQuery,
                                       String selectedRegionName) {

        session.setAttribute("lastTravelResultExists", true);
        session.setAttribute("lastTravelRequest", request);
        session.setAttribute("lastGptResult", gptResult);
        session.setAttribute("lastRecommendationResult", recommendationResult);
        session.setAttribute("lastCongestionAnalysis", congestionAnalysis);
        session.setAttribute("lastWeatherForecast", weatherForecast);
        session.setAttribute("lastWeatherForecasts", weatherForecasts);
        session.setAttribute("lastThemeCandidates", candidates == null ? "" : candidates.getThemeCandidatesText());
        session.setAttribute("lastPlaceCandidates", candidates == null ? "" : candidates.getPlaceCandidatesText());
        session.setAttribute("lastTargetPlaces", targetPlaces);
        session.setAttribute("lastTargetPlaceData", targetPlaces != null && !targetPlaces.isEmpty() ? targetPlaces.get(0) : null);
        session.setAttribute("lastRestaurants", restaurants);
        session.setAttribute("lastCafes", cafes);
        session.setAttribute("lastHotels", hotels);
        session.setAttribute("lastAttractions", attractions);
        session.setAttribute("lastNaverSearchBaseQuery", naverSearchBaseQuery);
        session.setAttribute("lastNearbySearchBaseQuery", nearbySearchBaseQuery);
        session.setAttribute("lastSelectedRegionName", selectedRegionName);
    }

    private void restoreLastTravelResult(HttpSession session, Model model) {

        model.addAttribute("request", session.getAttribute("lastTravelRequest"));
        model.addAttribute("gptResult", session.getAttribute("lastGptResult"));
        model.addAttribute("recommendationResult", session.getAttribute("lastRecommendationResult"));
        model.addAttribute("congestionAnalysis", session.getAttribute("lastCongestionAnalysis"));
        model.addAttribute("weatherForecast", session.getAttribute("lastWeatherForecast"));
        model.addAttribute("weatherForecasts", session.getAttribute("lastWeatherForecasts"));
        model.addAttribute("themeCandidates", session.getAttribute("lastThemeCandidates"));
        model.addAttribute("placeCandidates", session.getAttribute("lastPlaceCandidates"));
        model.addAttribute("targetPlaces", session.getAttribute("lastTargetPlaces"));
        model.addAttribute("targetPlaceData", session.getAttribute("lastTargetPlaceData"));
        model.addAttribute("restaurants", session.getAttribute("lastRestaurants"));
        model.addAttribute("cafes", session.getAttribute("lastCafes"));
        model.addAttribute("hotels", session.getAttribute("lastHotels"));
        model.addAttribute("attractions", session.getAttribute("lastAttractions"));
        model.addAttribute("naverSearchBaseQuery", session.getAttribute("lastNaverSearchBaseQuery"));
        model.addAttribute("nearbySearchBaseQuery", session.getAttribute("lastNearbySearchBaseQuery"));
        model.addAttribute("selectedRegionName", session.getAttribute("lastSelectedRegionName"));
        model.addAttribute("naverMapClientId", naverMapClientId);
    }

    private String resolveCurrentUserDisplayName(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "여행자";
        }

        return userRepository.findByUsername(authentication.getName())
                .map(user -> user.getNickname() == null || user.getNickname().isBlank()
                        ? user.getUsername()
                        : user.getNickname())
                .orElse(authentication.getName());
    }

    private List<NearbyPlaceDto> resolveTargetPlaces(String baseQuery,
                                                     String regionName,
                                                     String placeName) {

        List<String> queries = new ArrayList<>();
        addSearchQuery(queries, baseQuery);

        String simplifiedPlaceName = simplifyPlaceName(placeName);
        String safeRegionName = regionName == null ? "" : regionName.trim();

        if (!safeRegionName.isBlank() && !simplifiedPlaceName.isBlank()) {
            addSearchQuery(queries, "강원특별자치도 " + safeRegionName + " " + simplifiedPlaceName);
            addSearchQuery(queries, safeRegionName + " " + simplifiedPlaceName);
        }

        if (!simplifiedPlaceName.isBlank()) {
            addSearchQuery(queries, simplifiedPlaceName);
        }

        List<NearbyPlaceDto> fallbackCandidates = new ArrayList<>();

        for (String query : queries) {
            List<NearbyPlaceDto> candidates =
                    naverLocalSearchService.searchTargetPlace(query);

            if (candidates == null || candidates.isEmpty()) {
                continue;
            }

            addUniquePlaces(fallbackCandidates, candidates, 10);

            NearbyPlaceDto preferredTarget =
                    selectPreferredTargetPlace(candidates, regionName, placeName);

            if (preferredTarget != null) {
                List<NearbyPlaceDto> targetPlaces = new ArrayList<>();
                targetPlaces.add(preferredTarget);
                return targetPlaces;
            }
        }

        NearbyPlaceDto fallbackTarget =
                selectPreferredTargetPlace(fallbackCandidates, "", placeName);

        if (fallbackTarget == null) {
            return fallbackCandidates;
        }

        List<NearbyPlaceDto> targetPlaces = new ArrayList<>();
        targetPlaces.add(fallbackTarget);
        return targetPlaces;
    }

    private String buildNearbySearchBaseQuery(List<NearbyPlaceDto> targetPlaces,
                                              String regionName,
                                              String placeName) {

        StringBuilder query = new StringBuilder();
        String normalizedRegionName = regionName == null ? "" : regionName.trim();

        if (!normalizedRegionName.isBlank()) {
            query.append(normalizedRegionName);
        }

        String localAreaName = extractLocalAreaName(targetPlaces);

        if (!localAreaName.isBlank()
                && !normalizeSearchText(query.toString()).contains(normalizeSearchText(localAreaName))) {
            if (!query.isEmpty()) {
                query.append(" ");
            }

            query.append(localAreaName);
        }

        if (query.isEmpty()) {
            return "강원특별자치도";
        }

        return query.toString().trim();
    }

    private String extractLocalAreaName(List<NearbyPlaceDto> targetPlaces) {
        if (targetPlaces == null || targetPlaces.isEmpty()) {
            return "";
        }

        NearbyPlaceDto target = targetPlaces.get(0);
        String addressText = target.getRoadAddress();

        if (addressText == null || addressText.isBlank()) {
            addressText = target.getAddress();
        }

        if (addressText == null || addressText.isBlank()) {
            return "";
        }

        String[] parts = addressText.split("\\s+");

        for (String part : parts) {
            if (part.endsWith("읍")
                    || part.endsWith("면")
                    || part.endsWith("동")
                    || part.endsWith("리")) {
                return part;
            }
        }

        return "";
    }

    private NearbyPlaceDto selectPreferredTargetPlace(List<NearbyPlaceDto> candidates,
                                                     String regionName,
                                                     String placeName) {

        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        NearbyPlaceDto best = null;
        int bestScore = Integer.MIN_VALUE;

        for (NearbyPlaceDto candidate : candidates) {
            int score = scoreTargetCandidate(candidate, regionName, placeName);

            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }

        if (regionName != null && !regionName.isBlank() && bestScore < 50) {
            return null;
        }

        return best;
    }

    private int scoreTargetCandidate(NearbyPlaceDto candidate,
                                     String regionName,
                                     String placeName) {

        if (candidate == null) {
            return Integer.MIN_VALUE;
        }

        int score = 0;
        String addressText = joinText(candidate.getAddress(), candidate.getRoadAddress());
        String title = normalizeSearchText(candidate.getTitle());
        String normalizedPlaceName = normalizeSearchText(simplifyPlaceName(placeName));

        if (addressText.contains("강원")) {
            score += 30;
        }

        if (matchesRegion(candidate, regionName)) {
            score += 70;
        }

        if (!normalizedPlaceName.isBlank() && title.contains(normalizedPlaceName)) {
            score += 50;
        }

        for (String token : normalizedPlaceName.split(" ")) {
            if (!token.isBlank() && token.length() >= 2 && title.contains(token)) {
                score += 12;
            }
        }

        if (title.contains("마켓") && !normalizedPlaceName.contains("마켓")) {
            score -= 25;
        }

        if (!addressText.contains("강원")) {
            score -= 40;
        }

        return score;
    }

    private void addSearchQuery(List<String> queries, String query) {
        if (query == null || query.isBlank()) {
            return;
        }

        String trimmedQuery = query.trim();

        if (!queries.contains(trimmedQuery)) {
            queries.add(trimmedQuery);
        }
    }

    private void applySavedCounts(List<NearbyPlaceDto> places) {

        if (places == null || places.isEmpty()) {
            return;
        }

        for (NearbyPlaceDto place : places) {
            String placeName = cleanPlaceTitle(place.getTitle());

            if (placeName.isBlank()) {
                place.setSavedCount(0);
                continue;
            }

            place.setSavedCount(countSavedPlace(placeName));
        }
    }

    private long countSavedPlace(String placeName) {
        long count = travelCourseItemRepository.countByPlaceName(placeName);

        if (count == 0) {
            count = travelCourseItemRepository.countByPlaceNameContaining(placeName);
        }

        return count;
    }

    private Double resolveWeatherLatitude(Place selectedPlace,
                                          List<NearbyPlaceDto> targetPlaces) {

        if (selectedPlace != null && selectedPlace.getLatitude() != null) {
            return selectedPlace.getLatitude();
        }

        if (targetPlaces != null && !targetPlaces.isEmpty()) {
            return parseNaverCoordinate(targetPlaces.get(0).getMapy());
        }

        return null;
    }

    private Double resolveWeatherLongitude(Place selectedPlace,
                                           List<NearbyPlaceDto> targetPlaces) {

        if (selectedPlace != null && selectedPlace.getLongitude() != null) {
            return selectedPlace.getLongitude();
        }

        if (targetPlaces != null && !targetPlaces.isEmpty()) {
            return parseNaverCoordinate(targetPlaces.get(0).getMapx());
        }

        return null;
    }

    private Double parseNaverCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) {
            return null;
        }

        try {
            double value = Double.parseDouble(coordinate.trim());

            if (Math.abs(value) > 1000) {
                return value / 10000000.0;
            }

            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<NearbyPlaceDto> searchNearbyPlaces(String nearbySearchBaseQuery,
                                                    String regionName,
                                                    String category,
                                                    int limit) {

        List<NearbyPlaceDto> result = new ArrayList<>();
        List<String> queries = new ArrayList<>();

        addSearchQuery(queries, nearbySearchBaseQuery + " " + category);

        if ("식당".equals(category)) {
            addSearchQuery(queries, nearbySearchBaseQuery + " 맛집");
        } else if ("카페".equals(category)) {
            addSearchQuery(queries, nearbySearchBaseQuery + " 커피");
        } else if ("숙소".equals(category)) {
            addSearchQuery(queries, nearbySearchBaseQuery + " 펜션");
        } else if ("관광지".equals(category)) {
            addSearchQuery(queries, nearbySearchBaseQuery + " 가볼만한곳");
        }

        addSearchQuery(queries, buildRegionQuery(regionName, category));

        for (String query : queries) {
            List<NearbyPlaceDto> searched =
                    naverLocalSearchService.searchPlacesByKeyword(query);

            addUniquePlaces(result, searched, limit);

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    private String buildRegionQuery(String regionName, String category) {
        String safeRegionName = regionName == null ? "" : regionName.trim();
        String safeCategory = category == null ? "" : category.trim();

        if (safeRegionName.isBlank()) {
            return "강원 " + safeCategory;
        }

        return safeRegionName + " " + safeCategory;
    }

    private void keepNearbyPlaces(List<NearbyPlaceDto> places,
                                  double maxDistanceKm,
                                  int minKeepCount) {

        if (places == null || places.isEmpty()) {
            return;
        }

        List<NearbyPlaceDto> nearbyPlaces = places.stream()
                .filter(place -> place.getDistanceKm() <= maxDistanceKm)
                .toList();

        if (nearbyPlaces.size() >= minKeepCount) {
            places.clear();
            places.addAll(nearbyPlaces);
            return;
        }

        int keepCount = Math.min(minKeepCount, places.size());
        List<NearbyPlaceDto> closestPlaces = new ArrayList<>(places.subList(0, keepCount));
        places.clear();
        places.addAll(closestPlaces);
    }

    private void keepRegionPlaces(List<NearbyPlaceDto> places,
                                  String regionName,
                                  int minKeepCount) {

        if (places == null || places.isEmpty() || regionName == null || regionName.isBlank()) {
            return;
        }

        List<NearbyPlaceDto> regionPlaces = places.stream()
                .filter(place -> matchesRegion(place, regionName))
                .toList();

        if (regionPlaces.size() >= minKeepCount) {
            places.clear();
            places.addAll(regionPlaces);
        }
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

    private String cleanPlaceTitle(String title) {
        if (title == null) {
            return "";
        }

        return title
                .replaceAll("<[^>]*>", "")
                .replace("&amp;", "&")
                .trim();
    }

    private boolean matchesRegion(NearbyPlaceDto place, String regionName) {
        if (place == null || regionName == null || regionName.isBlank()) {
            return false;
        }

        String addressText = joinText(place.getAddress(), place.getRoadAddress());
        String normalizedAddress = normalizeSearchText(addressText);
        String normalizedRegion = normalizeSearchText(regionName);

        if (normalizedRegion.endsWith("시")
                || normalizedRegion.endsWith("군")
                || normalizedRegion.endsWith("구")) {
            normalizedRegion = normalizedRegion.substring(0, normalizedRegion.length() - 1);
        }

        return !normalizedRegion.isBlank()
                && normalizedAddress.contains(normalizedRegion);
    }

    private String simplifyPlaceName(String placeName) {
        if (placeName == null) {
            return "";
        }

        return cleanPlaceTitle(placeName)
                .replace("카페", "")
                .replace("관광지", "")
                .replace("맛집", "")
                .trim();
    }

    private String normalizeSearchText(String text) {
        if (text == null) {
            return "";
        }

        return cleanPlaceTitle(text)
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private String joinText(String... values) {
        StringBuilder sb = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                sb.append(value).append(" ");
            }
        }

        return sb.toString();
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
