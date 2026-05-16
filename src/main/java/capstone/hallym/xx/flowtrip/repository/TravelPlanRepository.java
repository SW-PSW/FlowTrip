package capstone.hallym.xx.flowtrip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import capstone.hallym.xx.flowtrip.entity.TravelPlan;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    List<TravelPlan> findByUserSessionIdOrderByCreatedAtDesc(String userSessionId);
}