package capstone.hallym.xx.flowtrip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import capstone.hallym.xx.flowtrip.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("select distinct t.moodGroup from Theme t where t.moodGroup is not null order by t.moodGroup")
    List<String> findDistinctMoodGroups();

    List<Theme> findByMoodGroup(String moodGroup);
}