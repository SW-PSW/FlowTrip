package capstone.hallym.xx.flowtrip.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import capstone.hallym.xx.flowtrip.dto.RecommendationCandidatesDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.entity.Theme;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.repository.ThemeRepository;

@Service
public class RecommendationCandidateService {

    private final ThemeRepository themeRepository;
    private final PlaceRepository placeRepository;

    public RecommendationCandidateService(ThemeRepository themeRepository,
                                          PlaceRepository placeRepository) {
        this.themeRepository = themeRepository;
        this.placeRepository = placeRepository;
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
                .limit(5)
                .toList();
    }

    private List<Place> findRankedPlaces(TravelRequestDto dto, List<Theme> matchedThemes) {
        List<Place> places;

        if (matchedThemes == null || matchedThemes.isEmpty()) {
            places = placeRepository.findAll();
        } else {
            places = placeRepository.findByThemeIn(matchedThemes);
        }

        return places.stream()
                .filter(place -> calculatePlaceScore(place, dto) > 0)
                .sorted(Comparator.comparingInt((Place place) -> calculatePlaceScore(place, dto)).reversed())
                .limit(10)
                .toList();
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

        return score;
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
            sb.append("- placeId: ").append(place.getPlaceId()).append("\n");
            sb.append("  score: ").append(calculatePlaceScore(place, dto)).append("\n");
            sb.append("  placeName: ").append(nullSafe(place.getPlaceName())).append("\n");
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
}