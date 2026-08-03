package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.specifications.Form058SpecificationFactory;
import uz.uzinfocom.isemid.features.report.dtos.response.Form058ExportStatusResponse;
import uz.uzinfocom.isemid.features.report.job.Form058ExportJob;
import uz.uzinfocom.isemid.features.report.job.Form058ExportJobService;
import uz.uzinfocom.isemid.features.report.service.Form058ExportService;
import uz.uzinfocom.isemid.features.report.utils.Form058ExcelWriter;
import uz.uzinfocom.isemid.features.user.models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form058ExportServiceImpl implements Form058ExportService {
    private final Form058Repository form058Repository;
    private final Form058SpecificationFactory specFactory;
    private final Form058ExcelWriter excelWriter;
    private final Form058ExportJobService form058ExportJobService;

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

    @Transactional
    @Override
    public ResponseEntity<?> allExportV2(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                         User user) {
        return buildExportResponseV2(fromDate, toDate, stateCode, cityCode, user, FilterType.ALL);
    }

    @Transactional
    @Override
    public ResponseEntity<?> receivedExportV2(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                              User user) {
        return buildExportResponseV2(fromDate, toDate, stateCode, cityCode, user, FilterType.RECEIVER);
    }

    @Override
    public ResponseEntity<?> allExportV3(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                         User user) {
        return buildExportResponseV3(fromDate, toDate, stateCode, cityCode, user, FilterType.ALL);
    }

    @Override
    public ResponseEntity<?> receivedExportV3(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                              User user) {
        return buildExportResponseV3(fromDate, toDate, stateCode, cityCode, user, FilterType.RECEIVER);
    }

    /**
     * V3: so'rov kelganda darhol javob qaytaradi, fayl esa fon jarayonida (bitta,
     * navbat bilan ishlaydigan thread'da) diskka batchlab yoziladi. Tayyor bo'lgan
     * fayl {@code Form058ExportJobService} keshida (report turi + filtrlar + tashkilot
     * bo'yicha, 5 daqiqa TTL) saqlanadi va qayta so'rov yuborilganda diskdan o'qib
     * qaytariladi.
     */
    private ResponseEntity<?> buildExportResponseV3(LocalDate fromDate, LocalDate toDate, String stateCode,
                                                     String cityCode, User user, FilterType filterType) {
        Form058ExportJob job = form058ExportJobService.getOrStart(filterType, fromDate, toDate, stateCode, cityCode, user);

        return switch (job.getStatus()) {
            case IN_PROGRESS -> ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new Form058ExportStatusResponse("IN_PROGRESS",
                            "Hisobot tayyorlanmoqda. Birozdan so'ng qayta so'rov yuboring."));
            case FAILED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Form058ExportStatusResponse("FAILED", job.getErrorMessage()));
            case COMPLETED -> {
                Path filePath = job.getFilePath();
                if (filePath == null || !Files.exists(filePath)) {
                    yield ResponseEntity.status(HttpStatus.ACCEPTED)
                            .body(new Form058ExportStatusResponse("IN_PROGRESS",
                                    "Hisobot qayta tayyorlanmoqda. Birozdan so'ng qayta so'rov yuboring."));
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentDisposition(ContentDisposition.attachment()
                        .filename(job.getFileName() != null ? job.getFileName() : "journal_060_" + LocalDate.now() + ".xlsx")
                        .build());
                headers.setContentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                yield ResponseEntity.ok().headers(headers).body(new FileSystemResource(filePath));
            }
        };
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

    /**
     * {@code buildExportResponse}ning N+1'siz varianti — {@code excelWriter.writeV2}
     * chaqiradi, u bemor/tashkilot/katalog ma'lumotlarini har qator uchun alohida
     * emas, batch qilib yuklaydi.
     */
    private ResponseEntity<?> buildExportResponseV2(LocalDate fromDate, LocalDate toDate, String stateCode,
                                                     String cityCode, User user, FilterType filterType) {
        try (Stream<Form058> formStream = form058Repository.streamAll(specFactory.build(fromDate, toDate, stateCode, cityCode, user, filterType))) {

            byte[] excelData = excelWriter.writeV2(formStream, fromDate, toDate, user.getCurrentOrganization().getName());

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
