package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.entity.AppUser;
import capstone.hallym.xx.flowtrip.entity.TravelCourseItem;
import capstone.hallym.xx.flowtrip.entity.TravelPlan;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.repository.TravelPlanRepository;
import capstone.hallym.xx.flowtrip.repository.UserRepository;

@Controller
public class MyTravelController {

    private final TravelPlanRepository travelPlanRepository;
    private final TravelCourseItemRepository travelCourseItemRepository;
    private final UserRepository userRepository;

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    public MyTravelController(TravelPlanRepository travelPlanRepository,
                              TravelCourseItemRepository travelCourseItemRepository,
                              UserRepository userRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.travelCourseItemRepository = travelCourseItemRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-travel")
    public String myTravelList(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        List<TravelPlan> travelPlans =
                travelPlanRepository.findByUserOrderByCreatedAtDesc(user);

        model.addAttribute("travelPlans", travelPlans);

        return "my-travel-list";
    }

    @GetMapping("/my-travel/{travelPlanId}")
    public String myTravelDetail(@PathVariable Long travelPlanId,
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
                        .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(travelPlanId);

        model.addAttribute("travelPlan", travelPlan);
        model.addAttribute("courseItems", courseItems);
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

        model.addAttribute("dayCount", dayCount);
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "my-travel-detail";
    }

    @PostMapping("/my-travel/{travelPlanId}/delete")
    public String deleteTravelPlan(@PathVariable Long travelPlanId,
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

        List<TravelCourseItem> courseItems =
                travelCourseItemRepository
                        .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(travelPlanId);

        travelCourseItemRepository.deleteAll(courseItems);
        travelPlanRepository.delete(travelPlan);

        return "redirect:/my-travel";
    }
}