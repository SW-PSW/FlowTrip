package capstone.hallym.xx.flowtrip.dto;

public class RecommendationCandidatesDto {

    private String themeCandidatesText;
    private String placeCandidatesText;

    public RecommendationCandidatesDto(String themeCandidatesText, String placeCandidatesText) {
        this.themeCandidatesText = themeCandidatesText;
        this.placeCandidatesText = placeCandidatesText;
    }

    public String getThemeCandidatesText() {
        return themeCandidatesText;
    }

    public void setThemeCandidatesText(String themeCandidatesText) {
        this.themeCandidatesText = themeCandidatesText;
    }

    public String getPlaceCandidatesText() {
        return placeCandidatesText;
    }

    public void setPlaceCandidatesText(String placeCandidatesText) {
        this.placeCandidatesText = placeCandidatesText;
    }
}