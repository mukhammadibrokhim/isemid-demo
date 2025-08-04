package uz.uzinfocom.isemid.features.form058.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;

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
public class Form058Response {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String filterType; // RECEIVER, AFFILIATION, NONE
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;
    private Integer mkb10UsageLimit;
    private FormStatus status;
    private PatientResponse patient;
    private Boolean labConfirmation; // Tashxisni laborator tasdiqlash
    private OrgMiniResponse hospitalPlace; // Yotqizilgan joyi
    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private LocalDateTime docSendDate; // document jo'natilgan sana
    private OrgMiniResponse senderOrganization; // Qayerdan (Yuboruvchi tashkilot)
    private OrgMiniResponse receiverOrganization; // Qayerga (Qabul qiluvchi tashkilot)
    private LocationResponse location;
    private CatalogMinResponse diseasePlace; // Qayerda zaharlangan yoki kasallangan
    private LocalDateTime initialReportDateTime;
    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar
    private String journalFormCode;
    private String comment;
    private String notifierFullName;
    //    @JsonIgnoreProperties("form058")
    private List<CardResponse> cards = new ArrayList<>();
    private String cardByFullName;
    private String notApproveComment;
}
