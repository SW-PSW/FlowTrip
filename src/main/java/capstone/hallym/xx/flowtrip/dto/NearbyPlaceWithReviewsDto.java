package capstone.hallym.xx.flowtrip.dto;

import java.util.List;

public class NearbyPlaceWithReviewsDto {

    private NearbyPlaceDto place;
    private List<BlogReviewDto> reviews;
    private List<PlaceImageDto> images;

    public NearbyPlaceWithReviewsDto() {
    }

    public NearbyPlaceWithReviewsDto(NearbyPlaceDto place,
                                     List<BlogReviewDto> reviews,
                                     List<PlaceImageDto> images) {
        this.place = place;
        this.reviews = reviews;
        this.images = images;
    }

    public NearbyPlaceDto getPlace() {
        return place;
    }

    public List<BlogReviewDto> getReviews() {
        return reviews;
    }

    public List<PlaceImageDto> getImages() {
        return images;
    }
}