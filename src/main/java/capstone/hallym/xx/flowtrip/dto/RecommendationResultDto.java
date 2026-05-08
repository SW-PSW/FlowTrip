package capstone.hallym.xx.flowtrip.dto;

public class RecommendationResultDto {

    private String selectedThemeName;
    private String selectedThemeId;
    private String recommendedPlaceName;
    private String recommendedPlaceId;
    private String reason;
    private String caution;
    private String alternativePlaceName;
    private String alternativePlaceId;

    public RecommendationResultDto() {
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
}