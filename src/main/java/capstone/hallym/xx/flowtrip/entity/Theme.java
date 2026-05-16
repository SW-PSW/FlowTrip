package capstone.hallym.xx.flowtrip.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "themes")
public class Theme {

    @Id
    @Column(name = "theme_id")
    private Long themeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "theme_order")
    private Integer themeOrder;

    @Column(name = "theme_name", nullable = false, length = 200)
    private String themeName;

    @Column(name = "theme_summary", columnDefinition = "TEXT")
    private String themeSummary;

    @Column(name = "primary_mood", length = 100)
    private String primaryMood;

    @Column(name = "mood_group", length = 100)
    private String moodGroup;

    @Column(name = "primary_activity_level", length = 100)
    private String primaryActivityLevel;

    @Column(name = "recommended_for", length = 200)
    private String recommendedFor;

    @Column(name = "transport_hint", length = 200)
    private String transportHint;

    @Column(name = "weather_fit", length = 200)
    private String weatherFit;

    // 테마 기반 코스 생성용 데이터
    @Column(name = "typical_route", columnDefinition = "TEXT")
    private String typicalRoute;

    @Column(name = "recommended_course", columnDefinition = "TEXT")
    private String recommendedCourse;

    @Column(name = "ai_search_context", columnDefinition = "TEXT")
    private String aiSearchContext;

    @OneToMany(mappedBy = "theme")
    private List<Place> places = new ArrayList<>();

    @OneToMany(mappedBy = "theme")
    private List<ThemeTag> themeTags = new ArrayList<>();

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Integer getThemeOrder() {
        return themeOrder;
    }

    public void setThemeOrder(Integer themeOrder) {
        this.themeOrder = themeOrder;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeSummary() {
        return themeSummary;
    }

    public void setThemeSummary(String themeSummary) {
        this.themeSummary = themeSummary;
    }

    public String getPrimaryMood() {
        return primaryMood;
    }

    public void setPrimaryMood(String primaryMood) {
        this.primaryMood = primaryMood;
    }

    public String getMoodGroup() {
        return moodGroup;
    }

    public void setMoodGroup(String moodGroup) {
        this.moodGroup = moodGroup;
    }

    public String getPrimaryActivityLevel() {
        return primaryActivityLevel;
    }

    public void setPrimaryActivityLevel(String primaryActivityLevel) {
        this.primaryActivityLevel = primaryActivityLevel;
    }

    public String getRecommendedFor() {
        return recommendedFor;
    }

    public void setRecommendedFor(String recommendedFor) {
        this.recommendedFor = recommendedFor;
    }

    public String getTransportHint() {
        return transportHint;
    }

    public void setTransportHint(String transportHint) {
        this.transportHint = transportHint;
    }

    public String getWeatherFit() {
        return weatherFit;
    }

    public void setWeatherFit(String weatherFit) {
        this.weatherFit = weatherFit;
    }

    public String getTypicalRoute() {
        return typicalRoute;
    }

    public void setTypicalRoute(String typicalRoute) {
        this.typicalRoute = typicalRoute;
    }

    public String getRecommendedCourse() {
        return recommendedCourse;
    }

    public void setRecommendedCourse(String recommendedCourse) {
        this.recommendedCourse = recommendedCourse;
    }

    public String getAiSearchContext() {
        return aiSearchContext;
    }

    public void setAiSearchContext(String aiSearchContext) {
        this.aiSearchContext = aiSearchContext;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public List<ThemeTag> getThemeTags() {
        return themeTags;
    }

    public void setThemeTags(List<ThemeTag> themeTags) {
        this.themeTags = themeTags;
    }
}