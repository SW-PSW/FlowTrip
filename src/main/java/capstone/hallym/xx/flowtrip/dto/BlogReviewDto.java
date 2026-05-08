package capstone.hallym.xx.flowtrip.dto;

public class BlogReviewDto {

    private String title;
    private String link;
    private String description;
    private String bloggerName;
    private String postDate;

    public BlogReviewDto() {
    }

    public BlogReviewDto(String title, String link, String description, String bloggerName, String postDate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.bloggerName = bloggerName;
        this.postDate = postDate;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getBloggerName() {
        return bloggerName;
    }

    public String getPostDate() {
        return postDate;
    }
}