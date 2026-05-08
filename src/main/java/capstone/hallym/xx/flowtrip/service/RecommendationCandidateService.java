package capstone.hallym.xx.flowtrip.service;

import java.util.ArrayList;
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
        List<Theme> matchedThemes = findMatchedThemes(dto);
        List<Place> matchedPlaces = findMatchedPlaces(dto, matchedThemes);

        String themeCandidatesText = buildThemeCandidatesText(matchedThemes);
        String placeCandidatesText = buildPlaceCandidatesText(matchedPlaces);

        return new RecommendationCandidatesDto(themeCandidatesText, placeCandidatesText);
    }

    private List<Theme> findMatchedThemes(TravelRequestDto dto) {
        List<Theme> themes = themeRepository.findByMoodGroup(dto.getMoodGroup());
        List<Theme> matchedThemes = new ArrayList<>();

        for (Theme theme : themes) {
            boolean activityMatch = contains(theme.getPrimaryActivityLevel(), dto.getActivityLevel());
            boolean companionMatch = contains(theme.getRecommendedFor(), dto.getCompanion());
            boolean transportMatch = contains(theme.getTransportHint(), dto.getTransport());

            if (activityMatch && companionMatch && transportMatch) {
                matchedThemes.add(theme);
            }
        }

        return matchedThemes;
    }

    private List<Place> findMatchedPlaces(TravelRequestDto dto, List<Theme> matchedThemes) {
        if (matchedThemes.isEmpty()) {
            return List.of();
        }

        List<Place> places = placeRepository.findByThemeIn(matchedThemes);
        List<Place> matchedPlaces = new ArrayList<>();

        for (Place place : places) {
            boolean activityMatch = contains(place.getActivityLevel(), dto.getActivityLevel());
            boolean companionMatch = contains(place.getSuitableFor(), dto.getCompanion());
            boolean transportMatch = contains(place.getMobility(), dto.getTransport());

            if (activityMatch && companionMatch && transportMatch) {
                matchedPlaces.add(place);
            }
        }

        return matchedPlaces;
    }

    private boolean contains(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }
        return dbValue.contains(userValue);
    }

    private String buildThemeCandidatesText(List<Theme> themes) {
        StringBuilder sb = new StringBuilder();

        if (themes.isEmpty()) {
            sb.append("없음");
            return sb.toString();
        }

        for (Theme theme : themes) {
            sb.append("- themeId: ").append(theme.getThemeId()).append("\n");
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

    private String buildPlaceCandidatesText(List<Place> places) {
        StringBuilder sb = new StringBuilder();

        if (places.isEmpty()) {
            sb.append("없음");
            return sb.toString();
        }

        for (Place place : places) {
            sb.append("- placeId: ").append(place.getPlaceId()).append("\n");
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