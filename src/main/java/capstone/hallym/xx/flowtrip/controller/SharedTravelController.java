package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

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
    public String sharedTravelList(Model model) {
        List<SharedTravelPost> posts =
                sharedTravelPostRepository.findAllByOrderByCreatedAtDesc();

        model.addAttribute("posts", posts);

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

        return "shared-travel-detail";
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

        return "redirect:/shared-travel";
    }
}