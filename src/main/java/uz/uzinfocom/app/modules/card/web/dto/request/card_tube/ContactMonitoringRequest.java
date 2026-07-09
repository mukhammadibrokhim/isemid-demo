package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDate;

public record ContactMonitoringRequest(
        Long id,
        @Size(max = 255) String fullName,
        LocalDate birthDate,
        Integer age,
        @Size(max = 64) String relationCode,
        @Size(max = 255) String workplaceOrStudyPlace,
        @Size(max = 255) String notificationReceiver,
        LocalDate diagnosisDate,
        @Size(max = 64) String contactStatusCode
) implements ChildRequest {
}
