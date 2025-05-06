package uz.uzinfocom.isemid.features.report.service;

import org.springframework.http.ResponseEntity;
import uz.uzinfocom.isemid.features.user.models.User;

public interface Form058ExportService {
    ResponseEntity<?> allExport(User user);

    ResponseEntity<?> receivedExport(User user);
}
