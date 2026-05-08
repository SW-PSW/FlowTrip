package capstone.hallym.xx.flowtrip.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.entity.Theme;
import capstone.hallym.xx.flowtrip.repository.PlaceRepository;
import capstone.hallym.xx.flowtrip.repository.ThemeRepository;

@Service
public class RecommendationExportService {

    private final ThemeRepository themeRepository;
    private final PlaceRepository placeRepository;

    public RecommendationExportService(ThemeRepository themeRepository,
                                       PlaceRepository placeRepository) {
        this.themeRepository = themeRepository;
        this.placeRepository = placeRepository;
    }

    public Path exportCandidatesToTxt(TravelRequestDto requestDto) throws IOException {
        List<Theme> matchedThemes = findMatchedThemes(requestDto);
        List<Place> matchedPlaces = findMatchedPlaces(requestDto, matchedThemes);

        String content = buildTxtContent(requestDto, matchedThemes, matchedPlaces);

        Path outputDir = Path.of("exports");
        Files.createDirectories(outputDir);

        String fileName = "recommendation_candidates_" + System.currentTimeMillis() + ".txt";
        Path outputPath = outputDir.resolve(fileName);

        Files.writeString(outputPath, content, StandardCharsets.UTF_8);

        return outputPath;
    }

    private List<Theme> findMatchedThemes(TravelRequestDto requestDto) {
        List<Theme> themes = themeRepository.findByMoodGroup(requestDto.getMoodGroup());
        List<Theme> matched = new ArrayList<>();

        for (Theme theme : themes) {
            boolean activityMatch = contains(theme.getPrimaryActivityLevel(), requestDto.getActivityLevel());
            boolean companionMatch = contains(theme.getRecommendedFor(), requestDto.getCompanion());
            boolean transportMatch = contains(theme.getTransportHint(), requestDto.getTransport());

            if (activityMatch && companionMatch && transportMatch) {
                matched.add(theme);
            }
        }

        return matched;
    }

    private List<Place> findMatchedPlaces(TravelRequestDto requestDto, List<Theme> matchedThemes) {
        if (matchedThemes.isEmpty()) {
            return List.of();
        }

        List<Place> places = placeRepository.findByThemeIn(matchedThemes);
        List<Place> matched = new ArrayList<>();

        for (Place place : places) {
            boolean activityMatch = contains(place.getActivityLevel(), requestDto.getActivityLevel());
            boolean companionMatch = contains(place.getSuitableFor(), requestDto.getCompanion());
            boolean transportMatch = contains(place.getMobility(), requestDto.getTransport());

            if (activityMatch && companionMatch && transportMatch) {
                matched.add(place);
            }
        }

        return matched;
    }

    private boolean contains(String dbValue, String userValue) {
        if (dbValue == null || userValue == null) {
            return false;
        }
        return dbValue.contains(userValue);
    }

    private String buildTxtContent(TravelRequestDto requestDto,
                                   List<Theme> themes,
                                   List<Place> places) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        sb.append("=== 사용자 입력 정보 ===\n");
        sb.append("여행 시작일: ").append(formatDate(requestDto.getStartDate(), formatter)).append("\n");
        sb.append("여행 종료일: ").append(formatDate(requestDto.getEndDate(), formatter)).append("\n");
        sb.append("액티비티 정도: ").append(nullSafe(requestDto.getActivityLevel())).append("\n");
        sb.append("동행 유형: ").append(nullSafe(requestDto.getCompanion())).append("\n");
        sb.append("이동수단: ").append(nullSafe(requestDto.getTransport())).append("\n");
        sb.append("여행 무드: ").append(nullSafe(requestDto.getMoodGroup())).append("\n");
        sb.append("특이사항: ").append(nullSafe(requestDto.getSpecialRequest())).append("\n\n");

        sb.append("=== 조건에 맞는 테마 목록 ===\n");
        if (themes.isEmpty()) {
            sb.append("일치하는 테마가 없습니다.\n");
        } else {
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
        }

        sb.append("=== 조건에 맞는 장소 목록 ===\n");
        if (places.isEmpty()) {
            sb.append("일치하는 장소가 없습니다.\n");
        } else {
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
        }

        return sb.toString();
    }

    private String formatDate(java.time.LocalDate date, DateTimeFormatter formatter) {
        return date == null ? "" : date.format(formatter);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}