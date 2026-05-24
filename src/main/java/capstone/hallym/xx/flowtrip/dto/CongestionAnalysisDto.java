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
    private String weatherSummary;
    private String weatherCrowdImpactReason;
    private int weatherCrowdImpactScore;
    private int naverLocalResultCount;
    private int naverReviewResultCount;
    private long savedCount;
    private int popularityImpactScore;

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

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public String getWeatherCrowdImpactReason() {
        return weatherCrowdImpactReason;
    }

    public void setWeatherCrowdImpactReason(String weatherCrowdImpactReason) {
        this.weatherCrowdImpactReason = weatherCrowdImpactReason;
    }

    public int getWeatherCrowdImpactScore() {
        return weatherCrowdImpactScore;
    }

    public void setWeatherCrowdImpactScore(int weatherCrowdImpactScore) {
        this.weatherCrowdImpactScore = weatherCrowdImpactScore;
    }

    public int getNaverLocalResultCount() {
        return naverLocalResultCount;
    }

    public void setNaverLocalResultCount(int naverLocalResultCount) {
        this.naverLocalResultCount = naverLocalResultCount;
    }

    public int getNaverReviewResultCount() {
        return naverReviewResultCount;
    }

    public void setNaverReviewResultCount(int naverReviewResultCount) {
        this.naverReviewResultCount = naverReviewResultCount;
    }

    public long getSavedCount() {
        return savedCount;
    }

    public void setSavedCount(long savedCount) {
        this.savedCount = savedCount;
    }

    public int getPopularityImpactScore() {
        return popularityImpactScore;
    }

    public void setPopularityImpactScore(int popularityImpactScore) {
        this.popularityImpactScore = popularityImpactScore;
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
