package capstone.hallym.xx.flowtrip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import capstone.hallym.xx.flowtrip.entity.TravelCourseItem;
import capstone.hallym.xx.flowtrip.entity.TravelPlan;

public interface TravelCourseItemRepository extends JpaRepository<TravelCourseItem, Long> {

    List<TravelCourseItem> findByTravelPlanOrderByCourseOrderAsc(TravelPlan travelPlan);

    List<TravelCourseItem> findByTravelPlanIdOrderByCourseOrderAsc(Long travelPlanId);

    List<TravelCourseItem> findByTravelPlanIdOrderByDayIndexAscCourseOrderAsc(Long travelPlanId);

    long countByPlaceName(String placeName);

    long countByPlaceNameContaining(String placeName);
}
