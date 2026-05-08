package capstone.hallym.xx.flowtrip.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @OneToMany(mappedBy = "tag")
    private List<PlaceTag> placeTags = new ArrayList<>();

    @OneToMany(mappedBy = "tag")
    private List<ThemeTag> themeTags = new ArrayList<>();

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public List<PlaceTag> getPlaceTags() {
        return placeTags;
    }

    public void setPlaceTags(List<PlaceTag> placeTags) {
        this.placeTags = placeTags;
    }

    public List<ThemeTag> getThemeTags() {
        return themeTags;
    }

    public void setThemeTags(List<ThemeTag> themeTags) {
        this.themeTags = themeTags;
    }
}