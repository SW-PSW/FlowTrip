package capstone.hallym.xx.flowtrip.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.entity.AppUser;
import capstone.hallym.xx.flowtrip.entity.SharedTravelPost;
import capstone.hallym.xx.flowtrip.entity.TravelCourseItem;
import capstone.hallym.xx.flowtrip.entity.TravelPlan;
import capstone.hallym.xx.flowtrip.repository.SharedTravelPostRepository;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.repository.TravelPlanRepository;
import capstone.hallym.xx.flowtrip.repository.UserRepository;

@Controller
public class SharedTravelController {

    private final SharedTravelPostRepository sharedTravelPostRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelCourseItemRepository travelCourseItemRepository;
    private final UserRepository userRepository;

    public SharedTravelController(SharedTravelPostRepository sharedTravelPostRepository,
                                  TravelPlanRepository travelPlanRepository,
                                  TravelCourseItemRepository travelCourseItemRepository,
                                  UserRepository userRepository) {
        this.sharedTravelPostRepository = sharedTravelPostRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.travelCourseItemRepository = travelCourseItemRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/shared-travel")
    public String sharedTravelList(@RequestParam(required = false) String place,
                                   Model model) {
        List<SharedTravelPost> posts =
                sharedTravelPostRepository.findAllByOrderByCreatedAtDesc();

        String filterPlace = place == null ? "" : place.trim();

        if (!filterPlace.isBlank()) {
            posts = filterPostsByRecommendedPlace(posts, filterPlace);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("earnedBadgesByUserId", buildEarnedBadgesByUserId(posts));
        model.addAttribute("filterPlace", filterPlace);

        return "shared-travel-list";
    }

    @GetMapping("/shared-travel/{postId}")
    public String sharedTravelDetail(@PathVariable Long postId,
                                     Model model) {

        SharedTravelPost post = sharedTravelPostRepository.findById(postId)
                .orElse(null);

        if (post == null) {
            return "redirect:/shared-travel";
        }

        post.increaseViewCount();
        sharedTravelPostRepository.save(post);

        TravelPlan travelPlan = post.getTravelPlan();

        List<TravelCourseItem> courseItems =
                travelCourseItemRepository
                        .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(
                                travelPlan.getId()
                        );

        model.addAttribute("post", post);
        model.addAttribute("travelPlan", travelPlan);
        model.addAttribute("courseItems", courseItems);
        model.addAttribute("dayCount", calculateDayCount(travelPlan, courseItems));
        model.addAttribute("earnedBadges", buildEarnedBadgesForUser(post.getUser()));

        return "shared-travel-detail";
    }

    @GetMapping("/shared-travel/users/{userId}")
    public String sharedTravelUserProfile(@PathVariable Long userId,
                                          Model model) {

        AppUser profileUser = userRepository.findById(userId)
                .orElse(null);

        if (profileUser == null) {
            return "redirect:/shared-travel";
        }

        List<SharedTravelPost> posts =
                sharedTravelPostRepository.findByUserOrderByCreatedAtDesc(profileUser);

        List<BadgeView> badges = buildBadges(posts);

        int earnedBadgeCount = 0;
        for (BadgeView badge : badges) {
            if (badge.isEarned()) {
                earnedBadgeCount++;
            }
        }

        String displayName = profileUser.getNickname() == null || profileUser.getNickname().isBlank()
                ? profileUser.getUsername()
                : profileUser.getNickname();

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("displayName", displayName);
        model.addAttribute("avatarInitial", displayName.substring(0, 1));
        model.addAttribute("posts", posts);
        model.addAttribute("badges", badges);
        model.addAttribute("earnedBadgeCount", earnedBadgeCount);
        model.addAttribute("totalBadgeCount", badges.size());

        return "shared-travel-profile";
    }

    @GetMapping("/my-travel/{travelPlanId}/share")
    public String shareForm(@PathVariable Long travelPlanId,
                            Authentication authentication,
                            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElse(null);

        if (travelPlan == null) {
            return "redirect:/my-travel";
        }

        if (travelPlan.getUser() == null ||
                !travelPlan.getUser().getId().equals(user.getId())) {
            return "redirect:/my-travel";
        }

        List<TravelCourseItem> courseItems =
                travelCourseItemRepository
                        .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(
                                travelPlanId
                        );

        model.addAttribute("travelPlan", travelPlan);
        model.addAttribute("courseItems", courseItems);
        model.addAttribute("dayCount", calculateDayCount(travelPlan, courseItems));

        return "shared-travel-form";
    }

    @PostMapping("/my-travel/{travelPlanId}/share")
    public String shareSubmit(@PathVariable Long travelPlanId,
                              @RequestParam String title,
                              @RequestParam String content,
                              Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElse(null);

        if (travelPlan == null) {
            return "redirect:/my-travel";
        }

        if (travelPlan.getUser() == null ||
                !travelPlan.getUser().getId().equals(user.getId())) {
            return "redirect:/my-travel";
        }

        SharedTravelPost post = new SharedTravelPost();
        post.setUser(user);
        post.setTravelPlan(travelPlan);
        post.setTitle(title);
        post.setContent(content);

        sharedTravelPostRepository.save(post);

        return "redirect:/shared-travel/" + post.getId();
    }

    private long calculateDayCount(TravelPlan travelPlan,
                                   List<TravelCourseItem> courseItems) {
        long dayCount = 1;

        if (travelPlan.getStartDate() != null && travelPlan.getEndDate() != null) {
            dayCount = java.time.temporal.ChronoUnit.DAYS.between(
                    travelPlan.getStartDate(),
                    travelPlan.getEndDate()
            ) + 1;
        }

        if (dayCount < 1) {
            dayCount = 1;
        }

        if (courseItems != null) {
            for (TravelCourseItem item : courseItems) {
                if (item.getDayIndex() != null && item.getDayIndex() > dayCount) {
                    dayCount = item.getDayIndex();
                }
            }
        }

        return dayCount;
    }

    private List<SharedTravelPost> filterPostsByRecommendedPlace(List<SharedTravelPost> posts,
                                                                  String place) {
        List<SharedTravelPost> filteredPosts = new ArrayList<>();
        String normalizedPlace = normalizeSearchText(place);

        if (normalizedPlace.isBlank()) {
            return posts;
        }

        for (SharedTravelPost post : posts) {
            if (post == null || post.getTravelPlan() == null) {
                continue;
            }

            String recommendedPlaceName =
                    post.getTravelPlan().getRecommendedPlaceName();
            String normalizedRecommendedPlace =
                    normalizeSearchText(recommendedPlaceName);

            if (normalizedRecommendedPlace.isBlank()) {
                continue;
            }

            if (normalizedRecommendedPlace.contains(normalizedPlace)
                    || normalizedPlace.contains(normalizedRecommendedPlace)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]*>", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "")
                .toLowerCase();
    }

    private List<BadgeView> buildBadges(List<SharedTravelPost> posts) {
        String corpus = buildUserTravelCorpus(posts);
        List<BadgeView> badges = new ArrayList<>();

        addBadge(badges, corpus, "바다 감성", "강릉", "커피 소환사",
                "안목해변 커피코스 방문 완료",
                List.of("강릉", "안목", "커피"));
        addBadge(badges, corpus, "바다 감성", "속초", "오징어 레이드 장인",
                "속초중앙시장 먹거리 코스 방문 완료",
                List.of("속초", "중앙시장", "먹거리"));
        addBadge(badges, corpus, "바다 감성", "동해", "해돋이 퍼스트 클리어",
                "추암촛대바위 일출 명소 코스 방문 완료",
                List.of("동해", "추암", "촛대바위", "일출"));
        addBadge(badges, corpus, "바다 감성", "삼척", "동굴 던전 마스터",
                "환선굴, 대금굴 동굴 탐험 코스 방문 완료",
                List.of("삼척", "환선굴", "대금굴", "동굴"));
        addBadge(badges, corpus, "바다 감성", "양양", "파도타기 만렙러",
                "서핑 해변 코스 방문 완료",
                List.of("양양", "서피비치", "죽도해변", "인구해변", "서핑"));
        addBadge(badges, corpus, "바다 감성", "고성", "DMZ 은신처 개척자",
                "통일전망대 방문 완료",
                List.of("고성", "통일전망대", "DMZ"));

        addBadge(badges, corpus, "자연 힐링", "평창", "눈꽃왕국 수호자",
                "대관령 목장 또는 설경 코스 방문 완료",
                List.of("평창", "대관령", "삼양라운드힐", "선자령", "목장"));
        addBadge(badges, corpus, "자연 힐링", "정선", "아리랑 소울 수집가",
                "정선 아리랑 코스 방문 완료",
                List.of("정선", "아리랑", "5일장"));
        addBadge(badges, corpus, "자연 힐링", "인제", "백패킹 생존 고수",
                "자작나무숲 트레킹 코스 방문 완료",
                List.of("인제", "자작나무숲", "트레킹"));
        addBadge(badges, corpus, "자연 힐링", "홍천", "숲멍 달인",
                "가리산, 수타사, 무궁화수목원 또는 은행나무숲 코스 방문 완료",
                List.of("홍천", "가리산", "수타사", "무궁화수목원", "은행나무숲"));
        addBadge(badges, corpus, "자연 힐링", "횡성", "한우 굽기 국가대표",
                "횡성 한우 코스 방문 완료",
                List.of("횡성", "한우"));
        addBadge(badges, corpus, "자연 힐링", "화천", "얼음낚시 전설러",
                "화천 산천어 코스 방문 완료",
                List.of("화천", "산천어", "얼음낚시"));

        addBadge(badges, corpus, "문화·먹거리", "춘천", "닭갈비 불쇼 지배자",
                "춘천 닭갈비 코스 방문 완료",
                List.of("춘천", "닭갈비", "닭불고기"));
        addBadge(badges, corpus, "문화·먹거리", "원주", "전시회 도장깨기러",
                "뮤지엄 산 또는 원주 전시 코스 방문 완료",
                List.of("원주", "뮤지엄 산", "시립미술관", "전시"));
        addBadge(badges, corpus, "문화·먹거리", "태백", "탄광 유물 발굴단장",
                "태백 체험공원 또는 석탄박물관 방문 완료",
                List.of("태백", "석탄", "탄광", "태백체험공원"));
        addBadge(badges, corpus, "문화·먹거리", "영월", "은하수 길잡이",
                "별마로천문대 방문 완료",
                List.of("영월", "별마로", "천문대", "은하수"));
        addBadge(badges, corpus, "문화·먹거리", "철원", "평화미션 수행자",
                "철원 평화전망대 방문 완료",
                List.of("철원", "평화전망대", "평화"));

        return badges;
    }

    private Map<Long, List<BadgeView>> buildEarnedBadgesByUserId(List<SharedTravelPost> posts) {
        Map<Long, List<BadgeView>> earnedBadgesByUserId = new HashMap<>();

        if (posts == null) {
            return earnedBadgesByUserId;
        }

        for (SharedTravelPost post : posts) {
            AppUser user = post.getUser();

            if (user == null || user.getId() == null || earnedBadgesByUserId.containsKey(user.getId())) {
                continue;
            }

            earnedBadgesByUserId.put(user.getId(), buildEarnedBadgesForUser(user));
        }

        return earnedBadgesByUserId;
    }

    private List<BadgeView> buildEarnedBadgesForUser(AppUser user) {
        if (user == null) {
            return List.of();
        }

        List<SharedTravelPost> userPosts =
                sharedTravelPostRepository.findByUserOrderByCreatedAtDesc(user);

        List<BadgeView> badges = buildBadges(userPosts);
        List<BadgeView> earnedBadges = new ArrayList<>();

        for (BadgeView badge : badges) {
            if (badge.isEarned()) {
                earnedBadges.add(badge);
            }
        }

        return earnedBadges;
    }

    private String buildUserTravelCorpus(List<SharedTravelPost> posts) {
        StringBuilder builder = new StringBuilder();

        if (posts == null) {
            return "";
        }

        for (SharedTravelPost post : posts) {
            appendText(builder, post.getTitle());
            appendText(builder, post.getContent());

            TravelPlan plan = post.getTravelPlan();

            if (plan == null) {
                continue;
            }

            appendText(builder, plan.getTitle());
            appendText(builder, plan.getRegion());
            appendText(builder, plan.getMoodGroup());
            appendText(builder, plan.getSelectedThemeName());
            appendText(builder, plan.getRecommendedPlaceName());
            appendText(builder, plan.getMemo());

            List<TravelCourseItem> courseItems =
                    travelCourseItemRepository
                            .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(plan.getId());

            for (TravelCourseItem item : courseItems) {
                appendText(builder, item.getPlaceName());
                appendText(builder, item.getCategory());
                appendText(builder, item.getAddress());
                appendText(builder, item.getMemo());
            }
        }

        return builder.toString().toLowerCase();
    }

    private void appendText(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(' ').append(value);
        }
    }

    private void addBadge(List<BadgeView> badges,
                          String corpus,
                          String theme,
                          String city,
                          String name,
                          String condition,
                          List<String> keywords) {
        boolean earned = false;

        for (String keyword : keywords) {
            if (corpus.contains(keyword.toLowerCase())) {
                earned = true;
                break;
            }
        }

        badges.add(new BadgeView(theme, city, name, condition, earned));
    }

    public static class BadgeView {

        private final String theme;
        private final String city;
        private final String name;
        private final String condition;
        private final boolean earned;
        private final String imagePath;

        public BadgeView(String theme,
                         String city,
                         String name,
                         String condition,
                         boolean earned) {
            this.theme = theme;
            this.city = city;
            this.name = name;
            this.condition = condition;
            this.earned = earned;
            this.imagePath = "/images/badges/" + resolveBadgeImageFileName(city);
        }

        public String getTheme() {
            return theme;
        }

        public String getCity() {
            return city;
        }

        public String getName() {
            return name;
        }

        public String getCondition() {
            return condition;
        }

        public boolean isEarned() {
            return earned;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getInitial() {
            return city == null || city.isBlank() ? "F" : city.substring(0, 1);
        }

        private static String resolveBadgeImageFileName(String city) {
            if ("강릉".equals(city)) return "gangneung.png";
            if ("고성".equals(city)) return "goseong.png";
            if ("동해".equals(city)) return "donghae.png";
            if ("삼척".equals(city)) return "samcheok.png";
            if ("속초".equals(city)) return "sokcho.png";
            if ("양양".equals(city)) return "yangyang.png";
            if ("영월".equals(city)) return "yeongwol.png";
            if ("원주".equals(city)) return "wonju.png";
            if ("인제".equals(city)) return "inje.png";
            if ("정선".equals(city)) return "jeongseon.png";
            if ("철원".equals(city)) return "cheorwon.png";
            if ("춘천".equals(city)) return "chuncheon.png";
            if ("태백".equals(city)) return "taebaek.png";
            if ("평창".equals(city)) return "pyeongchang.png";
            if ("홍천".equals(city)) return "hongcheon.png";
            if ("화천".equals(city)) return "hwacheon.png";
            if ("횡성".equals(city)) return "hoengseong.png";

            return "gangneung.png";
        }
    }
}
