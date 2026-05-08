package capstone.hallym.xx.flowtrip.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regions")
public class Region {

    @Id
    @Column(name = "region_id")
    private Long regionId;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "region_type", nullable = false, length = 20)
    private String regionType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "region")
    private List<Theme> themes = new ArrayList<>();

    @OneToMany(mappedBy = "region")
    private List<Place> places = new ArrayList<>();

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionType() {
        return regionType;
    }

    public void setRegionType(String regionType) {
        this.regionType = regionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }
}