package uz.uzinfocom.isemid.features.form058.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientMiniResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058MiniResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;
    private String source;
    private PatientMiniResponse patient;
    private FormStatus status;
    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private LocalDateTime initialReportDateTime;
    private LocalDateTime docSendDate;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;
    private OrgMiniResponse hospitalPlace;
    private String notifierFullName;
    private String cardByFullName;
}
