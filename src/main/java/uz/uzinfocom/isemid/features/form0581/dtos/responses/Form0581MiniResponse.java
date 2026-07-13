package uz.uzinfocom.isemid.features.form0581.dtos.responses;

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
public class Form0581MiniResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private String mkb10Code;
    private String mkb10Name;
    private String source;
    private PatientMiniResponse patient;
    private FormStatus status;
    private LocalDateTime injuryDateTime;
    private LocalDateTime visitDateTime;
    private LocalDateTime hospitalizedAt;
    private OrgMiniResponse hospitalPlace;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;
    private LocalDateTime initialReportDateTime;
    private String notifierFullName;
    private String receiverFullName;
}
