package capstone.hallym.xx.flowtrip.dto;

public class TravelCourseItemRequestDto {

    private String placeName;
    private String category;
    private String address;
    private String mapx;
    private String mapy;
    private String imageUrl;
    private Integer courseOrder;
    private Integer dayIndex;
    private String memo;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMapx() {
        return mapx;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }

    public String getMapy() {
        return mapy;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getCourseOrder() {
        return courseOrder;
    }

    public void setCourseOrder(Integer courseOrder) {
        this.courseOrder = courseOrder;
    }

    public Integer getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(Integer dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}