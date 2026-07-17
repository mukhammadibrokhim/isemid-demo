package uz.uzinfocom.isemid.features.form0581.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581DetailsResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean affiliation;
    private String source;
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;
    private FormStatus status;
    private PatientResponse patient;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;
    private LocalDateTime injuryDateTime;
    private LocalDateTime visitDateTime;
    private LocalDateTime hospitalizedAt;
    private OrgMiniResponse hospitalPlace;
    private LocalDateTime initialReportDateTime;
    private String notifierFullName;
    private String receiverFullName;
    private String cancelReason;
    private UserMiniResponse canceledBy;
    private String approvedFullName;
    private OrgMiniResponse approvedOrg;
}
