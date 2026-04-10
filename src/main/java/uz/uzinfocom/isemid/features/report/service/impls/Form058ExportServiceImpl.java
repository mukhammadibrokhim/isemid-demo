package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.specifications.Form058SpecificationFactory;
import uz.uzinfocom.isemid.features.report.service.Form058ExportService;
import uz.uzinfocom.isemid.features.report.utils.Form058ExcelWriter;
import uz.uzinfocom.isemid.features.user.models.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form058ExportServiceImpl implements Form058ExportService {
    private final Form058Repository form058Repository;
    private final Form058SpecificationFactory specFactory;
    private final Form058ExcelWriter excelWriter;

    @Transactional
    @Override
    public ResponseEntity<?> allExport(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                       User user) {
        return buildExportResponse(fromDate, toDate, stateCode, cityCode, user, FilterType.ALL);
    }

    @Transactional
    @Override
    public ResponseEntity<?> receivedExport(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                            User user) {
        return buildExportResponse(fromDate, toDate, stateCode, cityCode, user, FilterType.RECEIVER);
    }

    private ResponseEntity<?> buildExportResponse(LocalDate fromDate, LocalDate toDate, String stateCode,
                                                  String cityCode, User user, FilterType filterType) {
        try (Stream<Form058> formStream = form058Repository.streamAll(specFactory.build(fromDate, toDate, stateCode, cityCode, user, filterType))) {

            byte[] excelData = excelWriter.write(formStream, fromDate, toDate, user.getCurrentOrganization().getName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("journal_060_" + LocalDate.now() + ".xlsx").build());
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok().headers(headers).body(excelData);

        } catch (IOException e) {
            log.error("Excel export error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while writing to Excel.");
        }
    }
}
