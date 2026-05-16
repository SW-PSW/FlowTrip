package capstone.hallym.xx.flowtrip.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResultDto {

    private String selectedThemeName;
    private String selectedThemeId;

    private String recommendedPlaceName;
    private String recommendedPlaceId;

    private String reason;
    private String caution;

    private String alternativePlaceName;
    private String alternativePlaceId;

    private Integer congestionScore;
    private String congestionLevel;
    private String congestionReason;
    private String recommendedVisitTime;
    private String weatherAdvice;

    private String demandSignal;
    private String seasonSignal;
    private String weekendSignal;
    private String weatherSignal;
    private String alternativeGuide;

    private Boolean weekend;
    private Boolean peakSeason;

    private List<String> restaurantKeywords = new ArrayList<>();
    private List<String> cafeKeywords = new ArrayList<>();
    private List<String> hotelKeywords = new ArrayList<>();
    private List<String> attractionKeywords = new ArrayList<>();

    private List<String> travelRoute = new ArrayList<>();

    public RecommendationResultDto() {
    }

    public void applyCongestionAnalysis(CongestionAnalysisDto analysis) {
        if (analysis == null) {
            return;
        }

        this.congestionScore = analysis.getCongestionScore();
        this.congestionLevel = analysis.getCongestionLevel();
        this.congestionReason = analysis.getCongestionReason();
        this.recommendedVisitTime = analysis.getRecommendedVisitTime();

        this.demandSignal = analysis.getDemandSignal();
        this.seasonSignal = analysis.getSeasonSignal();
        this.weekendSignal = analysis.getWeekendSignal();
        this.weatherSignal = analysis.getWeatherSignal();
        this.alternativeGuide = analysis.getAlternativeGuide();

        this.weekend = analysis.isWeekend();
        this.peakSeason = analysis.isPeakSeason();
    }

    public String getSelectedThemeName() {
        return selectedThemeName;
    }

    public void setSelectedThemeName(String selectedThemeName) {
        this.selectedThemeName = selectedThemeName;
    }

    public String getSelectedThemeId() {
        return selectedThemeId;
    }

    public void setSelectedThemeId(String selectedThemeId) {
        this.selectedThemeId = selectedThemeId;
    }

    public String getRecommendedPlaceName() {
        return recommendedPlaceName;
    }

    public void setRecommendedPlaceName(String recommendedPlaceName) {
        this.recommendedPlaceName = recommendedPlaceName;
    }

    public String getRecommendedPlaceId() {
        return recommendedPlaceId;
    }

    public void setRecommendedPlaceId(String recommendedPlaceId) {
        this.recommendedPlaceId = recommendedPlaceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCaution() {
        return caution;
    }

    public void setCaution(String caution) {
        this.caution = caution;
    }

    public String getAlternativePlaceName() {
        return alternativePlaceName;
    }

    public void setAlternativePlaceName(String alternativePlaceName) {
        this.alternativePlaceName = alternativePlaceName;
    }

    public String getAlternativePlaceId() {
        return alternativePlaceId;
    }

    public void setAlternativePlaceId(String alternativePlaceId) {
        this.alternativePlaceId = alternativePlaceId;
    }

    public Integer getCongestionScore() {
        return congestionScore;
    }

    public void setCongestionScore(Integer congestionScore) {
        this.congestionScore = congestionScore;
    }

    public String getCongestionLevel() {
        return congestionLevel;
    }

    public void setCongestionLevel(String congestionLevel) {
        this.congestionLevel = congestionLevel;
    }

    public String getCongestionReason() {
        return congestionReason;
    }

    public void setCongestionReason(String congestionReason) {
        this.congestionReason = congestionReason;
    }

    public String getRecommendedVisitTime() {
        return recommendedVisitTime;
    }

    public void setRecommendedVisitTime(String recommendedVisitTime) {
        this.recommendedVisitTime = recommendedVisitTime;
    }

    public String getWeatherAdvice() {
        return weatherAdvice;
    }

    public void setWeatherAdvice(String weatherAdvice) {
        this.weatherAdvice = weatherAdvice;
    }

    public String getDemandSignal() {
        return demandSignal;
    }

    public void setDemandSignal(String demandSignal) {
        this.demandSignal = demandSignal;
    }

    public String getSeasonSignal() {
        return seasonSignal;
    }

    public void setSeasonSignal(String seasonSignal) {
        this.seasonSignal = seasonSignal;
    }

    public String getWeekendSignal() {
        return weekendSignal;
    }

    public void setWeekendSignal(String weekendSignal) {
        this.weekendSignal = weekendSignal;
    }

    public String getWeatherSignal() {
        return weatherSignal;
    }

    public void setWeatherSignal(String weatherSignal) {
        this.weatherSignal = weatherSignal;
    }

    public String getAlternativeGuide() {
        return alternativeGuide;
    }

    public void setAlternativeGuide(String alternativeGuide) {
        this.alternativeGuide = alternativeGuide;
    }

    public Boolean getWeekend() {
        return weekend;
    }

    public void setWeekend(Boolean weekend) {
        this.weekend = weekend;
    }

    public Boolean getPeakSeason() {
        return peakSeason;
    }

    public void setPeakSeason(Boolean peakSeason) {
        this.peakSeason = peakSeason;
    }

    public List<String> getRestaurantKeywords() {
        return restaurantKeywords;
    }

    public void setRestaurantKeywords(List<String> restaurantKeywords) {
        this.restaurantKeywords = restaurantKeywords;
    }

    public List<String> getCafeKeywords() {
        return cafeKeywords;
    }

    public void setCafeKeywords(List<String> cafeKeywords) {
        this.cafeKeywords = cafeKeywords;
    }

    public List<String> getHotelKeywords() {
        return hotelKeywords;
    }

    public void setHotelKeywords(List<String> hotelKeywords) {
        this.hotelKeywords = hotelKeywords;
    }

    public List<String> getAttractionKeywords() {
        return attractionKeywords;
    }

    public void setAttractionKeywords(List<String> attractionKeywords) {
        this.attractionKeywords = attractionKeywords;
    }

    public List<String> getTravelRoute() {
        return travelRoute;
    }

    public void setTravelRoute(List<String> travelRoute) {
        this.travelRoute = travelRoute;
    }
}