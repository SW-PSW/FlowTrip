package capstone.hallym.xx.flowtrip.dto;

public class CongestionAnalysisDto {

    private int congestionScore;
    private String congestionLevel;
    private String congestionReason;
    private String recommendedVisitTime;
    private String demandSignal;
    private String seasonSignal;
    private String weekendSignal;
    private String weatherSignal;
    private String alternativeGuide;

    private boolean weekend;
    private boolean peakSeason;

    public CongestionAnalysisDto() {
    }

    public int getCongestionScore() {
        return congestionScore;
    }

    public void setCongestionScore(int congestionScore) {
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

    public boolean isWeekend() {
        return weekend;
    }

    public void setWeekend(boolean weekend) {
        this.weekend = weekend;
    }

    public boolean isPeakSeason() {
        return peakSeason;
    }

    public void setPeakSeason(boolean peakSeason) {
        this.peakSeason = peakSeason;
    }
}