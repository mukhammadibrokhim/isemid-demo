package uz.uzinfocom.isemid.features.form058.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientMiniResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058MiniResponse {
    private Long id;
    private UUID uuid;
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;
    private PatientMiniResponse patient;
    private FormStatus status;
    private LocalDateTime docSendDate;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;
    private OrgMiniResponse hospitalPlace;
    private String notifierFullName;
    private String cardByFullName;
}
