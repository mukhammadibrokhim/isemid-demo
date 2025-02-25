package uz.uzinfocom.isemid.models.entities.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.*;
import uz.uzinfocom.isemid.models.entities.enums.DocumentStatus;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058Dto {

    private String mkb10Code;
    private String mkb10Name;
    private Integer mkb10UsageLimit;

    private DocumentStatus status;
//    @JsonIgnoreProperties({"identifiers", "birthDate", "ageYears", "state", "ageMonths", "gender", "phoneNumber",
//            "kinshipDegree", "kinshipFullName", "residentialStatus", "maritalStatus", "populationType", "category", "profession",
//            "workPlace", "workPlaceState", "workPlaceCity", "addresses", ""})
    private Patient patient;

    private Boolean labConfirmation; // Tashxisni laborator tasdiqlash
//    @JsonIgnoreProperties({"children", "serviceArea", "city", "state", "country", "level", "active", "email", "phone", "type",
//            "hierarchy", "district", "line", "parentId"})
    private Organization hospitalPlace; // Yotqizilgan joyi

    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private LocalDateTime docSendDate; // document jo'natilgan sana
//    @JsonIgnoreProperties({"children", "serviceArea", "city", "state", "country", "level", "active", "email", "phone", "type",
//            "hierarchy", "district", "line", "parentId"})
    private Organization senderOrganization; // Qayerdan (Yuboruvchi tashkilot)
//    @JsonIgnoreProperties({"children", "serviceArea", "city", "state", "country", "level", "active", "email", "phone", "type",
//            "hierarchy", "district", "line", "parentId"})
    private Organization receiverOrganization; // Qayerga (Qabul qiluvchi tashkilot)
//    @JsonIgnoreProperties({"createdAt", "updatedAt", "updatedBy", "createdBy"})
    private Location location;
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog diseasePlace; // Qayerda zaharlangan yoki kasallangan

    private LocalDateTime initialReportDateTime;

    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog journalForm;

    private String comment;
    private String notifierFullName;

    private List<CardDto> cards = new ArrayList<>();
//    @JsonIgnoreProperties({"organizations", "roles", "country", "state", "city", "gender", "birthDate", "active", "phoneNumber", "district", "line", "accessLevel", "position"})
    private User reporter;

    public static Form058Dto fromEntity(Form058 form058, boolean isAllApi) {
        return Form058Dto.builder()
                .mkb10Code(form058.getMkb10Code())
                .mkb10Name(form058.getMkb10Name())
                .mkb10UsageLimit(form058.getMkb10UsageLimit())
                .status(form058.getStatus())
                .patient(form058.getPatient())
                .labConfirmation(form058.getLabConfirmation())
                .hospitalPlace(form058.getHospitalPlace())
                .admissionDate(form058.getAdmissionDate())
                .diseaseDate(form058.getDiseaseDate())
                .firstVisitDate(form058.getFirstVisitDate())
                .diagnosisDate(form058.getDiagnosisDate())
                .visitDate(form058.getVisitDate())
                .docSendDate(form058.getDocSendDate())
                .senderOrganization(form058.getSenderOrganization())
                .receiverOrganization(form058.getReceiverOrganization())
                .location(form058.getLocation())
                .diseasePlace(form058.getDiseasePlace())
                .initialReportDateTime(form058.getInitialReportDateTime())
                .diseaseCause(form058.getDiseaseCause())
                .epidemicMeasures(form058.getEpidemicMeasures())
                .journalForm(form058.getJournalForm())
                .comment(form058.getComment())
                .notifierFullName(form058.getNotifierFullName())
                .reporter(form058.getReporter())
                .cards(isAllApi
                        ? form058.getCards().stream()
                        .map(CardDto::fromEntity)
                        .toList()
                        : form058.getCards().stream()
                        .map(card -> new CardDto(
                                card.getId(),
                                card.getUuid(),
                                card.getCardType().name()
                        ))
                        .toList())
                .build();
    }
}