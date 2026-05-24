package capstone.hallym.xx.flowtrip.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import capstone.hallym.xx.flowtrip.dto.RecommendationCandidatesDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.entity.Theme;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.repository.ThemeRepository;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;

@Service
public class RecommendationCandidateService {

    private final ThemeRepository themeRepository;
    private final PlaceRepository placeRepository;
    private final TravelCourseItemRepository travelCourseItemRepository;

    public RecommendationCandidateService(ThemeRepository themeRepository,
                                          PlaceRepository placeRepository,
                                          TravelCourseItemRepository travelCourseItemRepository) {
        this.themeRepository = themeRepository;
        this.placeRepository = placeRepository;
        this.travelCourseItemRepository = travelCourseItemRepository;
    }

    public RecommendationCandidatesDto findCandidates(TravelRequestDto dto) {
        List<Theme> matchedThemes = findRankedThemes(dto);
        List<Place> matchedPlaces = findRankedPlaces(dto, matchedThemes);

        String themeCandidatesText = buildThemeCandidatesText(matchedThemes, dto);
        String placeCandidatesText = buildPlaceCandidatesText(matchedPlaces, dto);

        return new RecommendationCandidatesDto(themeCandidatesText, placeCandidatesText);
    }

    private List<Theme> findRankedThemes(TravelRequestDto dto) {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .filter(theme -> calculateThemeScore(theme, dto) > 0)
                .sorted(Comparator.comparingInt((Theme theme) -> calculateThemeScore(theme, dto)).reversed())
                .limit(8)
                .toList();
    }

    private List<Place> findRankedPlaces(TravelRequestDto dto, List<Theme> matchedThemes) {
        List<Place> places;

        if (matchedThemes == null || matchedThemes.isEmpty()) {
            places = placeRepository.findAll();
        } else {
            places = placeRepository.findByThemeIn(matchedThemes);
        }

        List<PlaceCandidate> rankedCandidates = places.stream()
                .map(place -> new PlaceCandidate(
                        place,
                        calculatePlaceScore(place, dto),
                        countSavedPlace(place)
                ))
                .filter(candidate -> candidate.getBaseScore() > 0)
                .sorted(Comparator.comparingInt(PlaceCandidate::getBaseScore).reversed())
                .limit(45)
                .toList();

        return selectDiversePlaces(rankedCandidates, 14);
    }

    private int calculateThemeScore(Theme theme, TravelRequestDto dto) {
        int score = 0;

        if (contains(theme.getMoodGroup(), dto.getMoodGroup())) {
            score += 50;
        }

        if (isCompanionMatch(theme.getRecommendedFor(), dto.getCompanion())) {
            score += 20;
        }

        if (isActivityMatch(theme.getPrimaryActivityLevel(), dto.getActivityLevel())) {
            score += 15;
        }

        if (isTransportMatch(theme.getTransportHint(), dto.getTransport())) {
            score += 10;
        }

        return score;
    }

    private int calculatePlaceScore(Place place, TravelRequestDto dto) {
        int score = 0;

        if (contains(place.getMood(), dto.getMoodGroup())) {
            score += 35;
        }

        if (isCompanionMatch(place.getSuitableFor(), dto.getCompanion())) {
            score += 25;
        }

        if (isActivityMatch(place.getActivityLevel(), dto.getActivityLevel())) {
            score += 20;
        }

        if (isTransportMatch(place.getMobility(), dto.getTransport())) {
            score += 15;
        }

        if (contains(place.getWeatherFit(), "사계절")) {
            score += 5;
        }

        score += calculateSpecialRequestScore(place, dto);

        return score;
    }

    private List<Place> selectDiversePlaces(List<PlaceCandidate> candidates, int limit) {
        List<Place> selectedPlaces = new ArrayList<>();
        List<PlaceCandidate> remaining = new ArrayList<>(candidates);
        Map<String, Integer> regionCounts = new HashMap<>();
        Map<String, Integer> themeCounts = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        while (!remaining.isEmpty() && selectedPlaces.size() < limit) {
            PlaceCandidate best = null;
            int bestScore = Integer.MIN_VALUE;

            for (PlaceCandidate candidate : remaining) {
                int adjustedScore = calculateDiversityAdjustedScore(
                        candidate,
                        regionCounts,
                        themeCounts,
                        categoryCounts,
                        selectedPlaces.size()
                );

                if (adjustedScore > bestScore) {
                    best = candidate;
                    bestScore = adjustedScore;
                }
            }

            if (best == null) {
                break;
            }

            Place place = best.getPlace();
            selectedPlaces.add(place);
            increaseCount(regionCounts, getRegionKey(place));
            increaseCount(themeCounts, getThemeKey(place));
            increaseCount(categoryCounts, getCategoryKey(place));
            remaining.remove(best);
        }

        return selectedPlaces;
    }

    private int calculateDiversityAdjustedScore(PlaceCandidate candidate,
                                                Map<String, Integer> regionCounts,
                                                Map<String, Integer> themeCounts,
                                                Map<String, Integer> categoryCounts,
                                                int selectedCount) {
        Place place = candidate.getPlace();
        int score = candidate.getBaseScore();

        score -= getCount(regionCounts, getRegionKey(place)) * 14;
        score -= getCount(themeCounts, getThemeKey(place)) * 18;
        score -= getCount(categoryCounts, getCategoryKey(place)) * 10;

        long savedCount = candidate.getSavedCount();

        if (savedCount >= 8) {
            score -= 12;
        } else if (savedCount >= 4) {
            score -= 7;
        } else if (savedCount == 0 && selectedCount >= 3) {
            score += 8;
        }

        if (place.getPopularityScore() != null) {
            score += Math.min(place.getPopularityScore(), 20) / 4;
        }

        return score;
    }

    private int calculateSpecialRequestScore(Place place, TravelRequestDto dto) {
        if (dto == null
                || dto.getSpecialRequest() == null
                || dto.getSpecialRequest().isBlank()
                || place == null) {
            return 0;
        }

        String searchText = normalizeSearchText(
                nullSafe(place.getPlaceName())
                        + " "
                        + nullSafe(place.getPlaceCategory())
                        + " "
                        + nullSafe(place.getDescription())
                        + " "
                        + nullSafe(place.getMood())
                        + " "
                        + nullSafe(place.getSourceNote())
        );
        String[] tokens = dto.getSpecialRequest()
                .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
                .split("\\s+");
        int score = 0;

        for (String token : tokens) {
            String normalizedToken = normalizeSearchText(token);

            if (normalizedToken.length() < 2) {
                continue;
            }

            if (searchText.contains(normalizedToken)) {
                score += 8;
            }
        }

        return Math.min(score, 24);
    }

    private long countSavedPlace(Place place) {
        if (place == null || place.getPlaceName() == null || place.getPlaceName().isBlank()) {
            return 0;
        }

        long count = travelCourseItemRepository.countByPlaceName(place.getPlaceName());

        if (count == 0) {
            count = travelCourseItemRepository.countByPlaceNameContaining(place.getPlaceName());
        }

        return count;
    }

    private void increaseCount(Map<String, Integer> counts, String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        counts.put(key, getCount(counts, key) + 1);
    }

    private int getCount(Map<String, Integer> counts, String key) {
        if (key == null || key.isBlank()) {
            return 0;
        }

        return counts.getOrDefault(key, 0);
    }

    private String getRegionKey(Place place) {
        if (place == null || place.getRegion() == null) {
            return "";
        }

        return nullSafe(place.getRegion().getRegionName());
    }

    private String getThemeKey(Place place) {
        if (place == null || place.getTheme() == null) {
            return "";
        }

        return String.valueOf(place.getTheme().getThemeId());
    }

    private String getCategoryKey(Place place) {
        if (place == null) {
            return "";
        }

        return normalizeSearchText(place.getPlaceCategory());
    }

    private boolean contains(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }

        return dbValue.contains(userValue);
    }

    private boolean isActivityMatch(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }

        if (dbValue.contains(userValue)) {
            return true;
        }

        if (userValue.equals("중간") && dbValue.contains("낮음")) {
            return true;
        }

        if (userValue.equals("낮음") && dbValue.contains("중간")) {
            return true;
        }

        if (userValue.equals("높음") && dbValue.contains("중간")) {
            return true;
        }

        return false;
    }

    private boolean isTransportMatch(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }

        if (dbValue.contains(userValue)) {
            return true;
        }

        if (userValue.equals("대중교통") && dbValue.contains("자차+대중교통")) {
            return true;
        }

        if (userValue.equals("대중교통") && dbValue.contains("대중교통 가능")) {
            return true;
        }

        if (userValue.equals("자차") && dbValue.contains("자차 추천")) {
            return true;
        }

        return false;
    }

    private boolean isCompanionMatch(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }

        if (dbValue.contains(userValue)) {
            return true;
        }

        if (userValue.equals("연인") && (dbValue.contains("커플") || dbValue.contains("데이트"))) {
            return true;
        }

        if (userValue.equals("친구") && (dbValue.contains("우정") || dbValue.contains("친구"))) {
            return true;
        }

        if (userValue.equals("가족") && dbValue.contains("아이")) {
            return true;
        }

        return false;
    }

    private String buildThemeCandidatesText(List<Theme> themes, TravelRequestDto dto) {
        StringBuilder sb = new StringBuilder();

        if (themes.isEmpty()) {
            sb.append("없음");
            return sb.toString();
        }

        for (Theme theme : themes) {
            sb.append("- themeId: ").append(theme.getThemeId()).append("\n");
            sb.append("  score: ").append(calculateThemeScore(theme, dto)).append("\n");
            sb.append("  themeName: ").append(nullSafe(theme.getThemeName())).append("\n");
            sb.append("  themeSummary: ").append(nullSafe(theme.getThemeSummary())).append("\n");
            sb.append("  primaryMood: ").append(nullSafe(theme.getPrimaryMood())).append("\n");
            sb.append("  moodGroup: ").append(nullSafe(theme.getMoodGroup())).append("\n");
            sb.append("  primaryActivityLevel: ").append(nullSafe(theme.getPrimaryActivityLevel())).append("\n");
            sb.append("  recommendedFor: ").append(nullSafe(theme.getRecommendedFor())).append("\n");
            sb.append("  transportHint: ").append(nullSafe(theme.getTransportHint())).append("\n");
            sb.append("  weatherFit: ").append(nullSafe(theme.getWeatherFit())).append("\n\n");
        }

        return sb.toString();
    }

    private String buildPlaceCandidatesText(List<Place> places, TravelRequestDto dto) {
        StringBuilder sb = new StringBuilder();

        if (places.isEmpty()) {
            sb.append("없음");
            return sb.toString();
        }

        for (Place place : places) {
            long savedCount = countSavedPlace(place);
            sb.append("- placeId: ").append(place.getPlaceId()).append("\n");
            sb.append("  score: ").append(calculatePlaceScore(place, dto)).append("\n");
            sb.append("  savedCount: ").append(savedCount).append("\n");
            sb.append("  diversityHint: ").append(savedCount == 0 ? "덜 노출된 후보" : "사용자 저장 이력 있음").append("\n");
            sb.append("  placeName: ").append(nullSafe(place.getPlaceName())).append("\n");
            sb.append("  region: ").append(place.getRegion() == null ? "" : nullSafe(place.getRegion().getRegionName())).append("\n");
            sb.append("  category: ").append(nullSafe(place.getPlaceCategory())).append("\n");
            sb.append("  description: ").append(nullSafe(place.getDescription())).append("\n");
            sb.append("  suitableFor: ").append(nullSafe(place.getSuitableFor())).append("\n");
            sb.append("  mobility: ").append(nullSafe(place.getMobility())).append("\n");
            sb.append("  weatherFit: ").append(nullSafe(place.getWeatherFit())).append("\n");
            sb.append("  indoorOutdoor: ").append(nullSafe(place.getIndoorOutdoor())).append("\n");
            sb.append("  activityLevel: ").append(nullSafe(place.getActivityLevel())).append("\n");
            sb.append("  priceLevel: ").append(nullSafe(place.getPriceLevel())).append("\n");
            sb.append("  stayTime: ").append(nullSafe(place.getStayTime())).append("\n");
            sb.append("  mood: ").append(nullSafe(place.getMood())).append("\n");
            sb.append("  sourceNote: ").append(nullSafe(place.getSourceNote())).append("\n\n");
        }

        return sb.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeSearchText(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("\\s+", "")
                .toLowerCase();
    }

    private static class PlaceCandidate {

        private final Place place;
        private final int baseScore;
        private final long savedCount;

        PlaceCandidate(Place place, int baseScore, long savedCount) {
            this.place = place;
            this.baseScore = baseScore;
            this.savedCount = savedCount;
        }

        Place getPlace() {
            return place;
        }

        int getBaseScore() {
            return baseScore;
        }

        long getSavedCount() {
            return savedCount;
        }
    }
}
