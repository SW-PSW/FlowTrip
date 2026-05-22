package capstone.hallym.xx.flowtrip.dto;

import java.time.LocalDate;

public class WeatherForecastDto {

    private LocalDate forecastDate;
    private String weatherSummary;
    private String weatherCodeDescription;
    private Double temperatureMax;
    private Double temperatureMin;
    private Integer precipitationProbability;
    private Double precipitationSum;
    private Integer crowdImpactScore;
    private String crowdImpactReason;
    private String sourceName;

    public WeatherForecastDto() {
    }

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public String getWeatherCodeDescription() {
        return weatherCodeDescription;
    }

    public void setWeatherCodeDescription(String weatherCodeDescription) {
        this.weatherCodeDescription = weatherCodeDescription;
    }

    public Double getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(Double temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public Double getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(Double temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(Integer precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public Double getPrecipitationSum() {
        return precipitationSum;
    }

    public void setPrecipitationSum(Double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    public Integer getCrowdImpactScore() {
        return crowdImpactScore;
    }

    public void setCrowdImpactScore(Integer crowdImpactScore) {
        this.crowdImpactScore = crowdImpactScore;
    }

    public String getCrowdImpactReason() {
        return crowdImpactReason;
    }

    public void setCrowdImpactReason(String crowdImpactReason) {
        this.crowdImpactReason = crowdImpactReason;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
