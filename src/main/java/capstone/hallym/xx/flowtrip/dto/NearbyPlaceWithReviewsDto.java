package capstone.hallym.xx.flowtrip.dto;

import java.util.List;

public class NearbyPlaceWithReviewsDto {

    private NearbyPlaceDto place;
    private List<BlogReviewDto> reviews;

    public NearbyPlaceWithReviewsDto() {
    }

    public NearbyPlaceWithReviewsDto(NearbyPlaceDto place, List<BlogReviewDto> reviews) {
        this.place = place;
        this.reviews = reviews;
    }

    public NearbyPlaceDto getPlace() {
        return place;
    }

    public List<BlogReviewDto> getReviews() {
        return reviews;
    }
}