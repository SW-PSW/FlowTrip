package capstone.hallym.xx.flowtrip.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "travel_plans")
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_plan_id")
    private Long id;

    @Column(name = "user_session_id", length = 200)
    private String userSessionId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "transport", length = 100)
    private String transport;

    @Column(name = "companion", length = 100)
    private String companion;

    @Column(name = "mood_group", length = 100)
    private String moodGroup;

    @Column(name = "selected_theme_name", length = 200)
    private String selectedThemeName;

    @Column(name = "recommended_place_name", length = 200)
    private String recommendedPlaceName;

    @Column(name = "styles", length = 1000)
    private String styles;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TravelPlan() {
    }

    public TravelPlan(LocalDate startDate,
                      LocalDate endDate,
                      String region,
                      String transport,
                      String companion,
                      String styles) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.region = region;
        this.transport = transport;
        this.companion = companion;
        this.styles = styles;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.title == null || this.title.isBlank()) {
            this.title = "나의 여행 코스";
        }
    }

    public Long getId() {
        return id;
    }

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}