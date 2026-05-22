package capstone.hallym.xx.flowtrip.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import capstone.hallym.xx.flowtrip.dto.TravelCourseItemRequestDto;
import capstone.hallym.xx.flowtrip.dto.TravelCourseSaveRequestDto;
import capstone.hallym.xx.flowtrip.entity.AppUser;
import capstone.hallym.xx.flowtrip.entity.TravelCourseItem;
import capstone.hallym.xx.flowtrip.entity.TravelPlan;
import capstone.hallym.xx.flowtrip.repository.TravelCourseItemRepository;
import capstone.hallym.xx.flowtrip.repository.TravelPlanRepository;
import capstone.hallym.xx.flowtrip.repository.UserRepository;

@RestController
@RequestMapping("/api/travel-course")
public class TravelCourseController {

    private final TravelPlanRepository travelPlanRepository;
    private final TravelCourseItemRepository travelCourseItemRepository;
    private final UserRepository userRepository;

    public TravelCourseController(TravelPlanRepository travelPlanRepository,
                                  TravelCourseItemRepository travelCourseItemRepository,
                                  UserRepository userRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.travelCourseItemRepository = travelCourseItemRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/save")
    public Map<String, Object> saveCourse(@RequestBody TravelCourseSaveRequestDto request,
                                          Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            );
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "저장할 여행 코스가 없습니다."
            );
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        TravelPlan plan = new TravelPlan();

        plan.setUser(user);

        plan.setTitle(
                request.getTitle() == null || request.getTitle().isBlank()
                        ? "나의 FlowTrip 여행"
                        : request.getTitle()
        );

        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setTransport(request.getTransport());
        plan.setCompanion(request.getCompanion());
        plan.setMoodGroup(request.getMoodGroup());
        plan.setSelectedThemeName(request.getSelectedThemeName());
        plan.setRecommendedPlaceName(request.getRecommendedPlaceName());
        plan.setMemo(request.getMemo());

        TravelPlan savedPlan = travelPlanRepository.save(plan);

        int order = 1;

        for (TravelCourseItemRequestDto itemDto : request.getItems()) {
            TravelCourseItem item = new TravelCourseItem();

            item.setTravelPlan(savedPlan);
            item.setPlaceName(itemDto.getPlaceName());
            item.setCategory(itemDto.getCategory());
            item.setAddress(itemDto.getAddress());
            item.setMapx(itemDto.getMapx());
            item.setMapy(itemDto.getMapy());
            item.setImageUrl(itemDto.getImageUrl());
            item.setCourseOrder(
                    itemDto.getCourseOrder() == null
                            ? order
                            : itemDto.getCourseOrder()
            );
            item.setDayIndex(
                    itemDto.getDayIndex() == null
                            ? 1
                            : itemDto.getDayIndex()
            );
            item.setMemo(itemDto.getMemo());

            travelCourseItemRepository.save(item);

            order++;
        }

        return Map.of(
                "success", true,
                "message", "여행 코스가 저장되었습니다.",
                "travelPlanId", savedPlan.getId()
        );
    }

    @GetMapping("/my")
    public List<TravelPlan> getMyTravelPlans(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        return travelPlanRepository.findByUserOrderByCreatedAtDesc(user);
    }
}