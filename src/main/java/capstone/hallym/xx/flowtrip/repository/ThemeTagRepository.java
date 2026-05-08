package capstone.hallym.xx.flowtrip.repository;

import capstone.hallym.xx.flowtrip.entity.ThemeTag;
import capstone.hallym.xx.flowtrip.entity.ThemeTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeTagRepository extends JpaRepository<ThemeTag, ThemeTagId> {}