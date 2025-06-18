package uz.uzinfocom.isemid.features.card.dtos.requests.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactMonitoringRequest {
    private String fullName; // 1. Мулоқатдагилар Ф.И.Ш.
    private LocalDate birthDate; // Tug‘ilgan sanasi (12.12.2020)
    private Integer age; // 2. Туғилган йили (ёши)
    private String relationCode; // 3. Қариндошлик даражаси (spravichnik)
    private String workplaceOrStudyPlace; // 4. Иш жойи (ўқиш жойи)
    private String notificationReceiver; // 5. Силга қарши диспансер хабарномани ким қабул қилган
    private LocalDate diagnosisDate; // 6. Tashxis qo‘yilgan vaqt
    private String contactStatusCode; // 7. Мулоқатдагилар ҳолати (spravichnik)
}
