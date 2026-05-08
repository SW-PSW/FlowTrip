package capstone.hallym.xx.flowtrip.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlaceTagId implements Serializable {

    private Long place;
    private Long tag;

    public PlaceTagId() {
    }

    public PlaceTagId(Long place, Long tag) {
        this.place = place;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceTagId that)) return false;
        return Objects.equals(place, that.place) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(place, tag);
    }
}