package capstone.hallym.xx.flowtrip.entity;

import java.io.Serializable;
import java.util.Objects;

public class ThemeTagId implements Serializable {

    private Long theme;
    private Long tag;

    public ThemeTagId() {
    }

    public ThemeTagId(Long theme, Long tag) {
        this.theme = theme;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThemeTagId that)) return false;
        return Objects.equals(theme, that.theme) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, tag);
    }
}