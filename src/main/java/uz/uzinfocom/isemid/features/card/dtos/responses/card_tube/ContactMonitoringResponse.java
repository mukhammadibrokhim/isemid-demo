package uz.uzinfocom.isemid.features.card.dtos.responses.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactMonitoringResponse {
    private Long id;
    private String fullName; // F.I.Sh. (To‘liq ism sharifi)
    private LocalDate birthDate; // Tug‘ilgan sana (Date of Birth)
    private Integer age; // Yoshi
    private String relationCode; // Qarindoshlik darajasi (masalan: ota, ona, aka)
    private String workStudyPlace; // Ish yoki o‘qish joyi
    private String notifier; // Xabar olgan shaxs yoki muassasa
    private LocalDate diagDate; // Tashxis qo‘yilgan sana (Diagnosis Date)
    private String statusCode; // Aloqa holati (Active, Monitored, Infected va h.k.)
}
