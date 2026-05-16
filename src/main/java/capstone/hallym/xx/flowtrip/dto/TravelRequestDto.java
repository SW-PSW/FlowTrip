package capstone.hallym.xx.flowtrip.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TravelRequestDto {

    @NotNull(message = "여행 시작일을 선택해주세요.")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일을 선택해주세요.")
    private LocalDate endDate;

    @NotBlank(message = "액티비티 정도를 선택해주세요.")
    private String activityLevel;

    @NotBlank(message = "동행 유형을 선택해주세요.")
    private String companion;

    @NotBlank(message = "이동수단을 선택해주세요.")
    private String transport;

    @NotBlank(message = "여행 무드를 선택해주세요.")
    private String moodGroup;

    private String specialRequest;

    // 혼잡도 기반 추천용 추가 입력값
    private Boolean avoidCrowdedPlaces;

    private String preferredTime;

    private String travelStyle;

    private Integer budgetLevel;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getCompanion() {
        return companion;
    }

    public void setCompanion(String companion) {
        this.companion = companion;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getMoodGroup() {
        return moodGroup;
    }

    public void setMoodGroup(String moodGroup) {
        this.moodGroup = moodGroup;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }

    public Boolean getAvoidCrowdedPlaces() {
        return avoidCrowdedPlaces;
    }

    public void setAvoidCrowdedPlaces(Boolean avoidCrowdedPlaces) {
        this.avoidCrowdedPlaces = avoidCrowdedPlaces;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getTravelStyle() {
        return travelStyle;
    }

    public void setTravelStyle(String travelStyle) {
        this.travelStyle = travelStyle;
    }

    public Integer getBudgetLevel() {
        return budgetLevel;
    }

    public void setBudgetLevel(Integer budgetLevel) {
        this.budgetLevel = budgetLevel;
    }
}