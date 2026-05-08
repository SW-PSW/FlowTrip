package capstone.hallym.xx.flowtrip.repository;

import capstone.hallym.xx.flowtrip.entity.PlaceTag;
import capstone.hallym.xx.flowtrip.entity.PlaceTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagId> {}
