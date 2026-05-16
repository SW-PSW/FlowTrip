package capstone.hallym.xx.flowtrip.dto;

public class NearbyPlaceDto {

    private String title;
    private String category;
    private String address;
    private String roadAddress;
    private String telephone;
    private String link;
    private String mapx;
    private String mapy;
    private double distanceKm;
    private String imageUrl;

    public NearbyPlaceDto() {
    }

    public NearbyPlaceDto(String title, String category, String address, String roadAddress,
                          String telephone, String link, String mapx, String mapy) {
        this.title = title;
        this.category = category;
        this.address = address;
        this.roadAddress = roadAddress;
        this.telephone = telephone;
        this.link = link;
        this.mapx = mapx;
        this.mapy = mapy;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getLink() {
        return link;
    }

    public String getMapx() {
        return mapx;
    }

    public String getMapy() {
        return mapy;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}