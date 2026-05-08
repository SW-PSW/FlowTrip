package capstone.hallym.xx.flowtrip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import capstone.hallym.xx.flowtrip.entity.Place;
import capstone.hallym.xx.flowtrip.entity.Theme;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByThemeIn(List<Theme> themes);
}