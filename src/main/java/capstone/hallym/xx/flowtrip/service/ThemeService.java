package capstone.hallym.xx.flowtrip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import capstone.hallym.xx.flowtrip.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<String> getMoodGroups() {
        return themeRepository.findDistinctMoodGroups();
    }
}