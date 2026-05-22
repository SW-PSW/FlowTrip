package capstone.hallym.xx.flowtrip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import capstone.hallym.xx.flowtrip.entity.SharedTravelPost;

public interface SharedTravelPostRepository
        extends JpaRepository<SharedTravelPost, Long> {

    List<SharedTravelPost> findAllByOrderByCreatedAtDesc();
}