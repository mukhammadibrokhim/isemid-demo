package uz.uzinfocom.isemid.features.report.service;

import org.springframework.http.ResponseEntity;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form058ExportService {
    ResponseEntity<?> allExport(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User user
    );

    ResponseEntity<?> receivedExport(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User user
    );

    ResponseEntity<?> allExportV3(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User user
    );

    ResponseEntity<?> receivedExportV3(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User user
    );
}
