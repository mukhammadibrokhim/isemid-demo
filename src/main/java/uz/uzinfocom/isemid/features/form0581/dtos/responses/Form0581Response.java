package uz.uzinfocom.isemid.features.form0581.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581Response {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean affiliation;
    private String source;
    private String mkb10Code;
    private String mkb10Name;
    private Integer mkb10UsageLimit;
    private String finalMkb10Code;
    private String finalMkb10Name;
    private String injuryLocalization;
    private String medRecordId;
    private FormStatus status;
    private PatientResponse patient;
    private OrgMiniResponse senderOrganization;
    private OrgMiniResponse receiverOrganization;
    private LocalDateTime injuryDateTime;
    private LocalDateTime visitDateTime;
    private String injuryStateCode;
    private String injuryCityCode;
    private String injuryAddress;
    private AnimalOwnerResponse animalOwner;
    private String animalCategoryCode;
    private String animalColor;
    private String animalType;
    private String animalBreed;
    private Boolean otherPeopleBitten;
    @Builder.Default
    private List<BittenPersonResponse> bittenPersons = new ArrayList<>();
    private LocalDateTime hospitalizedAt;
    private OrgMiniResponse hospitalPlace;
    private String antirabicAidInfo;
    private String notifierFullName;
    private String receiverFullName;
    private LocalDateTime initialReportDateTime;
    private String cancelReason;
    private UserMiniResponse canceledBy;
    private String approvedFullName;
    private OrgMiniResponse approvedOrg;
    private String notApproveComment;
}
