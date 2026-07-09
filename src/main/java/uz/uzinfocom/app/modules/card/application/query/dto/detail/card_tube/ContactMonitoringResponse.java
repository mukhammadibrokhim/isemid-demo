package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import java.time.LocalDate;

public record ContactMonitoringResponse(
        Long id,
        String fullName,
        LocalDate birthDate,
        Integer age,
        String relationCode,
        String workplaceOrStudyPlace,
        String notificationReceiver,
        LocalDate diagnosisDate,
        String contactStatusCode
) {
}
