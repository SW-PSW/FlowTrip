package capstone.hallym.xx.flowtrip.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TravelCourseSaveRequestDto {

    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    private String transport;
    private String companion;
    private String moodGroup;

    private String selectedThemeName;
    private String recommendedPlaceName;

    private String memo;

    private List<TravelCourseItemRequestDto> items = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getCompanion() {
        return companion;
    }

    public void setCompanion(String companion) {
        this.companion = companion;
    }

    public String getMoodGroup() {
        return moodGroup;
    }

    public void setMoodGroup(String moodGroup) {
        this.moodGroup = moodGroup;
    }

    public String getSelectedThemeName() {
        return selectedThemeName;
    }

    public void setSelectedThemeName(String selectedThemeName) {
        this.selectedThemeName = selectedThemeName;
    }

    public String getRecommendedPlaceName() {
        return recommendedPlaceName;
    }

    public void setRecommendedPlaceName(String recommendedPlaceName) {
        this.recommendedPlaceName = recommendedPlaceName;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public List<TravelCourseItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<TravelCourseItemRequestDto> items) {
        this.items = items;
    }
}