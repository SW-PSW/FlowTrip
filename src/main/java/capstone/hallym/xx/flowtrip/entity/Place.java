package capstone.hallym.xx.flowtrip.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "places")
public class Place {

    @Id
    @Column(name = "place_id")
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(name = "place_category", length = 200)
    private String placeCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "suitable_for", length = 200)
    private String suitableFor;

    @Column(length = 200)
    private String mobility;

    @Column(name = "weather_fit", length = 200)
    private String weatherFit;

    @Column(name = "indoor_outdoor", length = 50)
    private String indoorOutdoor;

    @Column(name = "activity_level", length = 100)
    private String activityLevel;

    @Column(name = "price_level", length = 100)
    private String priceLevel;

    @Column(name = "stay_time", length = 100)
    private String stayTime;

    @Column(length = 200)
    private String mood;

    @Column(name = "source_note", columnDefinition = "TEXT")
    private String sourceNote;

    @OneToMany(mappedBy = "place")
    private List<PlaceTag> placeTags = new ArrayList<>();

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceCategory() {
        return placeCategory;
    }

    public void setPlaceCategory(String placeCategory) {
        this.placeCategory = placeCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuitableFor() {
        return suitableFor;
    }

    public void setSuitableFor(String suitableFor) {
        this.suitableFor = suitableFor;
    }

    public String getMobility() {
        return mobility;
    }

    public void setMobility(String mobility) {
        this.mobility = mobility;
    }

    public String getWeatherFit() {
        return weatherFit;
    }

    public void setWeatherFit(String weatherFit) {
        this.weatherFit = weatherFit;
    }

    public String getIndoorOutdoor() {
        return indoorOutdoor;
    }

    public void setIndoorOutdoor(String indoorOutdoor) {
        this.indoorOutdoor = indoorOutdoor;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getStayTime() {
        return stayTime;
    }

    public void setStayTime(String stayTime) {
        this.stayTime = stayTime;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getSourceNote() {
        return sourceNote;
    }

    public void setSourceNote(String sourceNote) {
        this.sourceNote = sourceNote;
    }

    public List<PlaceTag> getPlaceTags() {
        return placeTags;
    }

    public void setPlaceTags(List<PlaceTag> placeTags) {
        this.placeTags = placeTags;
    }
}