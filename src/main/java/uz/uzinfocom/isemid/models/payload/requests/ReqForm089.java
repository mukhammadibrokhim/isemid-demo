package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqForm089 {

    @NotBlank(message = "Mkb10 cannot be blank and required.")
    private String mkb10;

    @NotBlank(message = "Mkb10 name cannot be blank and required.")
    private String mkb10Name;

    @NotBlank(message = "MedicalInstitution is required and cannot be blank.")
    private String medicalInstitution;

    @NotBlank(message = "DocumentSentDate is required and cannot be blank.")
    private String documentSentDate;

    @NotBlank(message = "DocumentSerialNumber is required and cannot be blank.")
    private String documentSerialNumber;

    @NotBlank(message = "BarcodeFill is required and cannot be blank.")
    private String barcodeFill;

    @Valid
    private ReqPatient patient;

    @NotBlank(message = "LivingPlace is required and cannot be blank.")
    private String livingPlace;

    @NotBlank(message = "FamilyStatus is required and cannot be blank.")
    private String familyStatus;

    @NotBlank(message = "SocialProfessionalStatus is required and cannot be blank.")
    private String socialProfessionalStatus;

    @NotBlank(message = "PopulationCategory is required and cannot be blank.")
    private String populationCategory;

    @NotBlank(message = "RiskGroup is required and cannot be blank.")
    private String riskGroup;

    @NotNull(message = "LastFluorographyDate is required.")
    private LocalDate lastFluorographyDate;

    @NotBlank(message = "DiseaseDetectionLocation is required and cannot be blank.")
    private String diseaseDetectionLocation;

    @NotNull(message = "FirstMedicalVisitDate is required.")
    private LocalDate firstMedicalVisitDate;

    @NotNull(message = "TuberculosisRegistrationDate is required.")
    private LocalDate tuberculosisRegistrationDate;

    @NotBlank(message = "DiseaseDetectionMethods is required and cannot be blank.")
    private String diseaseDetectionMethods;

    @NotBlank(message = "DetectedInMonitoredGroups is required and cannot be blank.")
    private String detectedInMonitoredGroups;

    @NotBlank(message = "DetectionMethod is required and cannot be blank.")
    private String detectionMethod;

    @NotBlank(message = "Diagnosis is required and cannot be blank.")
    private String diagnosis;

    @NotBlank(message = "PresenceOfDecay is required and cannot be blank.")
    private String presenceOfDecay;

    @NotBlank(message = "ConfirmationMethod is required and cannot be blank.")
    private String confirmationMethod;

    @NotBlank(message = "AssociatedDiseases is required and cannot be blank.")
    private String associatedDiseases;

    private boolean registeredInNarcologicalDispensary;

    @NotNull(message = "TuberculosisDiagnosisConfirmationDate is required.")
    private LocalDate tuberculosisDiagnosisConfirmationDate;

    @NotNull(message = "NotificationCompletionDate is required.")
    private LocalDate notificationCompletionDate;
}
