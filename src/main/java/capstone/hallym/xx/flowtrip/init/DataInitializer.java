package capstone.hallym.xx.flowtrip.init;

import capstone.hallym.xx.flowtrip.service.ExcelImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ExcelImportService excelImportService;

    public DataInitializer(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        excelImportService.importExcel();
    }
}