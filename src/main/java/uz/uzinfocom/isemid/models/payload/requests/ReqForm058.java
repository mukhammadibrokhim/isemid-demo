package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqForm058 {

    @NotBlank(message = "Mkb10 cannot be blank and required.")
    private String mkb10Code;

    @NotBlank(message = "Mkb10 name cannot be blank and required.")
    private String mkb10Name;

    @Valid
    @NotNull(message = "Patient object is required.")
    private ReqPatient patient;

    @NotBlank(message = "WorkPlace is required and cannot be blank.")
    private String workPlace;

    @NotBlank(message = "HospitalPlace is required and cannot be blank.")
    private String hospitalPlace;

    @NotNull(message = "AdmissionDate is required.")
    private LocalDate admissionDate;

    @NotNull(message = "DiseaseDate is required.")
    private LocalDate diseaseDate;

    @NotNull(message = "FirstVisitDate is required.")
    private LocalDate firstVisitDate;

    @NotNull(message = "DiagnosisDate is required.")
    private LocalDate diagnosisDate;

    @NotNull(message = "VisitDate is required.")
    private LocalDate visitDate;

    @NotBlank(message = "SenderOrganization is required and cannot be blank.")
    private String senderOrganization;

    @NotBlank(message = "ReceiverOrganization is required and cannot be blank.")
    private String receiverOrganization;

    @NotBlank(message = "PoisoningLocation is required and cannot be blank.")
    private String poisoningLocation;

    @NotBlank(message = "DiseaseCause is required and cannot be blank.")
    private String diseaseCause;

    @NotBlank(message = "EpidemicMeasures is required and cannot be blank.")
    private String epidemicMeasures;

    @NotBlank(message = "NotifierFullName cannot be blank.")
    private String notifierFullName;

    private String comment;
}
