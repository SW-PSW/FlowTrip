package capstone.hallym.xx.flowtrip.service;

import capstone.hallym.xx.flowtrip.entity.*;
import capstone.hallym.xx.flowtrip.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelImportService {

    private final RegionRepository regionRepository;
    private final TagRepository tagRepository;
    private final ThemeRepository themeRepository;
    private final PlaceRepository placeRepository;
    private final PlaceTagRepository placeTagRepository;
    private final ThemeTagRepository themeTagRepository;

    public ExcelImportService(
            RegionRepository regionRepository,
            TagRepository tagRepository,
            ThemeRepository themeRepository,
            PlaceRepository placeRepository,
            PlaceTagRepository placeTagRepository,
            ThemeTagRepository themeTagRepository
    ) {
        this.regionRepository = regionRepository;
        this.tagRepository = tagRepository;
        this.themeRepository = themeRepository;
        this.placeRepository = placeRepository;
        this.placeTagRepository = placeTagRepository;
        this.themeTagRepository = themeTagRepository;
    }

    @Transactional
    public void importExcel() throws Exception {
        try (InputStream is = new ClassPathResource("data/flowtrip_db.xlsx").getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            if (regionRepository.count() > 0) {
                importMissingPlaces(workbook.getSheet("places"));
                return;
            }

            importRegions(workbook.getSheet("regions"));
            importTags(workbook.getSheet("tags"));
            importThemes(workbook.getSheet("themes"));
            importPlaces(workbook.getSheet("places"));
            importPlaceTags(workbook.getSheet("place_tags"));
            importThemeTags(workbook.getSheet("theme_tags"));
        }
    }

    private void importRegions(Sheet sheet) {
        List<Region> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Region r = new Region();
            r.setRegionId((long) getInt(row.getCell(0)));
            r.setProvince(getString(row.getCell(1)));
            r.setRegionName(getString(row.getCell(2)));
            r.setRegionType(getString(row.getCell(3)));
            r.setDescription(getString(row.getCell(4)));

            list.add(r);
        }

        regionRepository.saveAll(list);
    }

    private void importTags(Sheet sheet) {
        List<Tag> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Tag t = new Tag();
            t.setTagId((long) getInt(row.getCell(0)));
            t.setTagName(getString(row.getCell(1)));

            list.add(t);
        }

        tagRepository.saveAll(list);
    }

    private void importThemes(Sheet sheet) {
        List<Theme> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long regionId = (long) getInt(row.getCell(1));
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionId));

            Theme t = new Theme();
            t.setThemeId((long) getInt(row.getCell(0)));
            t.setRegion(region);
            t.setThemeOrder(getInt(row.getCell(2)));
            t.setThemeName(getString(row.getCell(3)));
            t.setThemeSummary(getString(row.getCell(4)));
            t.setPrimaryMood(getString(row.getCell(5)));
            t.setMoodGroup(getString(row.getCell(6)));
            t.setPrimaryActivityLevel(getString(row.getCell(7)));
            t.setRecommendedFor(getString(row.getCell(8)));
            t.setTransportHint(getString(row.getCell(9)));
            t.setWeatherFit(getString(row.getCell(10)));

            list.add(t);
        }

        themeRepository.saveAll(list);
    }

    private void importPlaces(Sheet sheet) {
        importPlaces(sheet, false);
    }

    private void importMissingPlaces(Sheet sheet) {
        importPlaces(sheet, true);
    }

    private void importPlaces(Sheet sheet, boolean onlyMissing) {
        List<Place> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long placeId = (long) getInt(row.getCell(0));
            if (onlyMissing && placeRepository.existsById(placeId)) {
                continue;
            }

            Long regionId = (long) getInt(row.getCell(1));
            Long themeId = (long) getInt(row.getCell(2));

            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionId));

            Theme theme = themeRepository.findById(themeId)
                    .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + themeId));

            Place p = new Place();
            p.setPlaceId(placeId);
            p.setRegion(region);
            p.setTheme(theme);
            p.setPlaceName(getString(row.getCell(3)));
            p.setPlaceCategory(getString(row.getCell(4)));
            p.setDescription(getString(row.getCell(5)));
            p.setSuitableFor(getString(row.getCell(6)));
            p.setMobility(getString(row.getCell(7)));
            p.setWeatherFit(getString(row.getCell(8)));
            p.setIndoorOutdoor(getString(row.getCell(9)));
            p.setActivityLevel(getString(row.getCell(10)));
            p.setPriceLevel(getString(row.getCell(11)));
            p.setStayTime(getString(row.getCell(12)));
            p.setMood(getString(row.getCell(13)));
            p.setSourceNote(getString(row.getCell(14)));

            list.add(p);
        }

        placeRepository.saveAll(list);
    }

    private void importPlaceTags(Sheet sheet) {
        List<PlaceTag> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long placeId = (long) getInt(row.getCell(0));
            Long tagId = (long) getInt(row.getCell(1));

            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

            PlaceTag pt = new PlaceTag();
            pt.setPlace(place);
            pt.setTag(tag);

            list.add(pt);
        }

        placeTagRepository.saveAll(list);
    }

    private void importThemeTags(Sheet sheet) {
        List<ThemeTag> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long themeId = (long) getInt(row.getCell(0));
            Long tagId = (long) getInt(row.getCell(1));

            Theme theme = themeRepository.findById(themeId)
                    .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + themeId));

            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

            ThemeTag tt = new ThemeTag();
            tt.setTheme(theme);
            tt.setTag(tag);

            list.add(tt);
        }

        themeTagRepository.saveAll(list);
    }

    private String getString(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue()).trim();
        }

        return cell.toString().trim();
    }

    private int getInt(Cell cell) {
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        return Integer.parseInt(cell.toString().trim());
    }
}
