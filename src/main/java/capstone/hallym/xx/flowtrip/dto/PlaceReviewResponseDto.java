package capstone.hallym.xx.flowtrip.dto;

import java.util.List;

public class PlaceReviewResponseDto {

    private String placeName;
    private List<BlogReviewDto> reviews;
    private List<PlaceImageDto> images;

    public PlaceReviewResponseDto() {
    }

    public PlaceReviewResponseDto(
            String placeName,
            List<BlogReviewDto> reviews,
            List<PlaceImageDto> images
    ) {
        this.placeName = placeName;
        this.reviews = reviews;
        this.images = images;
    }

    public String getPlaceName() {
        return placeName;
    }

    public List<BlogReviewDto> getReviews() {
        return reviews;
    }

    public List<PlaceImageDto> getImages() {
        return images;
    }
}