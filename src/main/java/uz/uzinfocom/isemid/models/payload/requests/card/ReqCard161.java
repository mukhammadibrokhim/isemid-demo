package uz.uzinfocom.isemid.models.payload.requests.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD161 type card")
public class ReqCard161 extends ReqCard {
    private String residentialTreatmentFacility;
    private String region;
    private String district;
    private String polyclinic;
    private String initialSymptoms;
    //detected catalogga qoshish kerak
    private String detectedCode;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak
    private String deliveryMethodCode;
    private String homeStayExclusionReasonCode;
    private String lateAdmissionReasonsCode;

    private String diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private String estimatedInfectionDate;

    private List<String> riskFactorCodes;

    private String addressLocation;
    private String seasonTime;
    private String fullName;
    private String diagnosisClinicalFormOrDonorStatus;
}
