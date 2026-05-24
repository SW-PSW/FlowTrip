package capstone.hallym.xx.flowtrip.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import capstone.hallym.xx.flowtrip.dto.CongestionAnalysisDto;
import capstone.hallym.xx.flowtrip.dto.NearbyPlaceDto;
import capstone.hallym.xx.flowtrip.dto.RecommendationResultDto;
import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;
import capstone.hallym.xx.flowtrip.dto.WeatherForecastDto;
import capstone.hallym.xx.flowtrip.entity.Place;

@Service
public class CongestionAnalysisService {

    public CongestionAnalysisDto analyze(TravelRequestDto requestDto,
                                         RecommendationResultDto recommendationResult,
                                         Place selectedPlace,
                                         WeatherForecastDto weatherForecast,
                                         NearbyPlaceDto targetPlace,
                                         int naverReviewResultCount,
                                         long savedCount) {

        CongestionAnalysisDto result = new CongestionAnalysisDto();

        int score = 0;

        boolean weekend = isWeekend(requestDto.getStartDate());
        boolean peakSeason = isPeakSeason(requestDto.getStartDate(), selectedPlace);
        boolean popularPlace = isPopularPlace(selectedPlace);
        boolean outdoorPlace = isOutdoorPlace(selectedPlace);
        boolean seaOrLakePlace = isSeaOrLakePlace(selectedPlace);
        boolean avoidCrowded = Boolean.TRUE.equals(requestDto.getAvoidCrowdedPlaces());

        if (popularPlace) {
            score += 25;
        }

        if (weekend) {
            score += 20;
        }

        if (peakSeason) {
            score += 25;
        }

        if (outdoorPlace) {
            score += 10;
        }

        if (seaOrLakePlace) {
            score += 10;
        }

        if (avoidCrowded && score >= 50) {
            score += 10;
        }

        int naverLocalResultCount = targetPlace == null
                ? 0
                : targetPlace.getNaverLocalResultCount();

        int popularityImpactScore = calculatePopularityImpactScore(
                naverLocalResultCount,
                naverReviewResultCount,
                savedCount
        );

        score += popularityImpactScore;

        int weatherCrowdImpactScore = 0;
        String weatherCrowdImpactReason = "날씨 예보가 없어 혼잡도 보정에는 반영하지 않았습니다.";

        if (weatherForecast != null) {
            weatherCrowdImpactScore = weatherForecast.getCrowdImpactScore() == null
                    ? 0
                    : weatherForecast.getCrowdImpactScore();
            weatherCrowdImpactReason = weatherForecast.getCrowdImpactReason();
            score += weatherCrowdImpactScore;
        }

        score = Math.max(0, Math.min(score, 100));

        String level = toLevel(score);

        result.setCongestionScore(score);
        result.setCongestionLevel(level);
        result.setWeekend(weekend);
        result.setPeakSeason(peakSeason);

        result.setWeekendSignal(buildWeekendSignal(weekend));
        result.setSeasonSignal(buildSeasonSignal(peakSeason, selectedPlace));
        result.setDemandSignal(buildDemandSignal(popularPlace, selectedPlace));
        result.setWeatherSignal(buildWeatherSignal(selectedPlace, weatherForecast));
        result.setWeatherSummary(weatherForecast == null ? null : weatherForecast.getWeatherSummary());
        result.setWeatherCrowdImpactReason(weatherCrowdImpactReason);
        result.setWeatherCrowdImpactScore(weatherCrowdImpactScore);
        result.setNaverLocalResultCount(naverLocalResultCount);
        result.setNaverReviewResultCount(naverReviewResultCount);
        result.setSavedCount(savedCount);
        result.setPopularityImpactScore(popularityImpactScore);
        result.setRecommendedVisitTime(buildRecommendedVisitTime(level, outdoorPlace));
        result.setAlternativeGuide(buildAlternativeGuide(level, recommendationResult));

        result.setCongestionReason(buildReason(
                score,
                level,
                weekend,
                peakSeason,
                popularPlace,
                outdoorPlace,
                seaOrLakePlace,
                avoidCrowded,
                naverLocalResultCount,
                naverReviewResultCount,
                savedCount,
                popularityImpactScore,
                weatherCrowdImpactScore,
                weatherCrowdImpactReason
        ));

        return result;
    }

    private boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return dayOfWeek == DayOfWeek.SATURDAY
                || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isPeakSeason(LocalDate date, Place place) {
        if (date == null) {
            return false;
        }

        int month = date.getMonthValue();

        if (month == 7 || month == 8 || month == 10 || month == 12 || month == 1) {
            return true;
        }

        if (place == null || place.getSeasonalPeak() == null) {
            return false;
        }

        String seasonalPeak = place.getSeasonalPeak();

        if (seasonalPeak.contains("봄") && (month == 3 || month == 4 || month == 5)) {
            return true;
        }

        if (seasonalPeak.contains("여름") && (month == 6 || month == 7 || month == 8)) {
            return true;
        }

        if (seasonalPeak.contains("가을") && (month == 9 || month == 10 || month == 11)) {
            return true;
        }

        if (seasonalPeak.contains("겨울") && (month == 12 || month == 1 || month == 2)) {
            return true;
        }

        return false;
    }

    private boolean isPopularPlace(Place place) {
        if (place == null) {
            return false;
        }

        if (place.getPopularityScore() != null && place.getPopularityScore() >= 70) {
            return true;
        }

        String text = joinText(
                place.getPlaceName(),
                place.getPlaceCategory(),
                place.getDescription(),
                place.getSourceNote()
        );

        return containsAny(text, "핫플", "대표", "유명", "명소", "인기", "시장", "해변", "스카이워크", "케이블카");
    }

    private boolean isOutdoorPlace(Place place) {
        if (place == null) {
            return false;
        }

        String text = joinText(
                place.getIndoorOutdoor(),
                place.getPlaceCategory(),
                place.getDescription()
        );

        return containsAny(text, "야외", "해변", "호수", "산", "공원", "거리", "시장", "전망", "스카이워크");
    }

    private boolean isSeaOrLakePlace(Place place) {
        if (place == null) {
            return false;
        }

        String text = joinText(
                place.getPlaceName(),
                place.getPlaceCategory(),
                place.getDescription(),
                place.getMood()
        );

        return containsAny(text, "바다", "해변", "해수욕장", "호수", "호", "강", "수변", "오션", "리버");
    }

    private String toLevel(int score) {
        if (score >= 70) {
            return "높음";
        }

        if (score >= 40) {
            return "보통";
        }

        return "낮음";
    }

    private int calculatePopularityImpactScore(int naverLocalResultCount,
                                               int naverReviewResultCount,
                                               long savedCount) {
        int score = 0;

        if (naverLocalResultCount >= 100) {
            score += 12;
        } else if (naverLocalResultCount >= 30) {
            score += 8;
        } else if (naverLocalResultCount >= 10) {
            score += 5;
        } else if (naverLocalResultCount > 0) {
            score += 2;
        }

        if (naverReviewResultCount >= 100) {
            score += 15;
        } else if (naverReviewResultCount >= 30) {
            score += 10;
        } else if (naverReviewResultCount >= 10) {
            score += 6;
        } else if (naverReviewResultCount > 0) {
            score += 3;
        }

        if (savedCount >= 20) {
            score += 15;
        } else if (savedCount >= 10) {
            score += 10;
        } else if (savedCount >= 3) {
            score += 5;
        } else if (savedCount > 0) {
            score += 2;
        }

        return Math.min(score, 30);
    }

    private String buildWeekendSignal(boolean weekend) {
        if (weekend) {
            return "주말 방문 일정으로 인해 평일보다 방문 수요가 높을 가능성이 있습니다.";
        }

        return "평일 방문 일정으로 주말 대비 혼잡 부담은 낮은 편입니다.";
    }

    private String buildSeasonSignal(boolean peakSeason, Place place) {
        if (peakSeason) {
            return "여행 시기가 계절성 수요가 높은 기간에 해당할 수 있습니다.";
        }

        if (place != null && place.getWeatherFit() != null && place.getWeatherFit().contains("사계절")) {
            return "사계절 방문 가능한 장소로 특정 계절에만 수요가 몰리는 정도는 비교적 낮습니다.";
        }

        return "뚜렷한 성수기 신호는 낮지만, 현장 상황에 따라 유동적일 수 있습니다.";
    }

    private String buildDemandSignal(boolean popularPlace, Place place) {
        if (popularPlace) {
            return "장소명·분류·설명 기준으로 대표 관광지 또는 인기 장소 성격이 강합니다.";
        }

        return "대형 대표 관광지보다는 상대적으로 분산 방문이 가능한 장소로 판단됩니다.";
    }

    private String buildWeatherSignal(Place place, WeatherForecastDto weatherForecast) {
        if (weatherForecast != null && weatherForecast.getWeatherSummary() != null) {
            return weatherForecast.getWeatherSummary();
        }

        if (place == null) {
            return "장소 특성 기반 날씨 영향 정보가 부족합니다.";
        }

        if (place.getIndoorOutdoor() != null && place.getIndoorOutdoor().contains("실내")) {
            return "실내 성격이 있어 비나 추위의 영향을 비교적 적게 받을 수 있습니다.";
        }

        if (place.getIndoorOutdoor() != null && place.getIndoorOutdoor().contains("야외")) {
            return "야외 성격이 있어 맑은 날과 주말에는 방문 수요가 증가할 수 있습니다.";
        }

        if (place.getWeatherFit() != null && !place.getWeatherFit().isBlank()) {
            return "날씨 적합 정보: " + place.getWeatherFit();
        }

        return "날씨 조건에 따라 체감 만족도와 방문 수요가 달라질 수 있습니다.";
    }

    private String buildRecommendedVisitTime(String level, boolean outdoorPlace) {
        if ("높음".equals(level)) {
            return "오전 10시 이전 또는 오후 5시 이후 방문을 권장합니다.";
        }

        if ("보통".equals(level)) {
            return "점심 직후 시간대는 피하고, 오전 늦은 시간이나 오후 늦은 시간대를 추천합니다.";
        }

        if (outdoorPlace) {
            return "날씨가 좋은 시간대에 여유롭게 방문하기 좋습니다.";
        }

        return "큰 혼잡 부담 없이 일정 중간에 배치하기 좋습니다.";
    }

    private String buildAlternativeGuide(String level, RecommendationResultDto recommendationResult) {
        if (!"높음".equals(level)) {
            return "현재 추천지는 일정에 포함하기 무리가 크지 않습니다.";
        }

        if (recommendationResult != null
                && recommendationResult.getAlternativePlaceName() != null
                && !recommendationResult.getAlternativePlaceName().isBlank()) {
            return "혼잡이 부담된다면 대체 장소인 "
                    + recommendationResult.getAlternativePlaceName()
                    + " 방문을 고려할 수 있습니다.";
        }

        return "혼잡이 부담된다면 동일 지역의 덜 알려진 주변 관광지나 실내형 장소를 함께 고려하는 것이 좋습니다.";
    }

    private String buildReason(int score,
                               String level,
                               boolean weekend,
                               boolean peakSeason,
                               boolean popularPlace,
                               boolean outdoorPlace,
                               boolean seaOrLakePlace,
                               boolean avoidCrowded,
                               int naverLocalResultCount,
                               int naverReviewResultCount,
                               long savedCount,
                               int popularityImpactScore,
                               int weatherCrowdImpactScore,
                               String weatherCrowdImpactReason) {

        StringBuilder sb = new StringBuilder();

        sb.append("혼잡도 점수는 ").append(score).append("점으로, 예상 혼잡도는 '")
                .append(level).append("'입니다. ");

        if (weekend) {
            sb.append("주말 일정이 반영되어 방문 수요 증가 가능성이 있습니다. ");
        }

        if (peakSeason) {
            sb.append("여행 시기가 성수기 또는 계절 수요가 높은 시기에 해당할 수 있습니다. ");
        }

        if (popularPlace) {
            sb.append("추천 장소가 대표 관광지 또는 인기 장소 성격을 가지고 있어 기본 수요가 높게 평가되었습니다. ");
        }

        if (outdoorPlace) {
            sb.append("야외 관광지 특성상 날씨가 좋을수록 방문객이 몰릴 수 있습니다. ");
        }

        if (seaOrLakePlace) {
            sb.append("바다·호수·강 등 수변 관광 요소가 있어 특정 계절과 주말에 수요가 집중될 수 있습니다. ");
        }

        if (avoidCrowded) {
            sb.append("사용자가 혼잡 회피를 선호하므로 혼잡 위험을 보수적으로 반영했습니다. ");
        }

        if (popularityImpactScore > 0) {
            sb.append("네이버 지역 검색 결과 ")
                    .append(naverLocalResultCount)
                    .append("건, 블로그 후기 검색 결과 ")
                    .append(naverReviewResultCount)
                    .append("건, 서비스 내 담은 수 ")
                    .append(savedCount)
                    .append("건을 수요 신호로 보아 +")
                    .append(popularityImpactScore)
                    .append("점을 반영했습니다. ");
        }

        if (weatherCrowdImpactScore != 0 || (weatherCrowdImpactReason != null && !weatherCrowdImpactReason.isBlank())) {
            sb.append("일기예보 보정(")
                    .append(weatherCrowdImpactScore >= 0 ? "+" : "")
                    .append(weatherCrowdImpactScore)
                    .append("점): ")
                    .append(weatherCrowdImpactReason)
                    .append(" ");
        }

        if (sb.length() == 0) {
            return "혼잡도 판단에 필요한 정보가 충분하지 않아 기본 수준으로 평가했습니다.";
        }

        return sb.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
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
}
