package capstone.hallym.xx.flowtrip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.entity.TravelCourseItem;
import capstone.hallym.xx.flowtrip.entity.TravelPlan;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.repository.TravelPlanRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class MyTravelController {

    private final TravelPlanRepository travelPlanRepository;
    private final TravelCourseItemRepository travelCourseItemRepository;

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    public MyTravelController(TravelPlanRepository travelPlanRepository,
                              TravelCourseItemRepository travelCourseItemRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.travelCourseItemRepository = travelCourseItemRepository;
    }

    @GetMapping("/my-travel")
    public String myTravelList(HttpSession session, Model model) {
        String sessionId = session.getId();

        List<TravelPlan> travelPlans =
                travelPlanRepository.findByUserSessionIdOrderByCreatedAtDesc(sessionId);

        model.addAttribute("travelPlans", travelPlans);

        return "my-travel-list";
    }

    @GetMapping("/my-travel/{travelPlanId}")
    public String myTravelDetail(@PathVariable Long travelPlanId,
                                 HttpSession session,
                                 Model model) {

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElse(null);

        if (travelPlan == null) {
            return "redirect:/my-travel";
        }

        String sessionId = session.getId();

        if (travelPlan.getUserSessionId() != null
                && !travelPlan.getUserSessionId().equals(sessionId)) {
            return "redirect:/my-travel";
        }

        List<TravelCourseItem> courseItems =
                travelCourseItemRepository
                        .findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(travelPlanId);

        model.addAttribute("travelPlan", travelPlan);
        model.addAttribute("courseItems", courseItems);
        model.addAttribute("naverMapClientId", naverMapClientId);

        return "my-travel-detail";
    }

    @PostMapping("/my-travel/{travelPlanId}/delete")
    public String deleteTravelPlan(@PathVariable Long travelPlanId,
                                   HttpSession session) {

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElse(null);

        if (travelPlan == null) {
            return "redirect:/my-travel";
        }

        String sessionId = session.getId();

        if (travelPlan.getUserSessionId() != null
                && !travelPlan.getUserSessionId().equals(sessionId)) {
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