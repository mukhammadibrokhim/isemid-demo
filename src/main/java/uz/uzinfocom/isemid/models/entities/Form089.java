package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDate;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form089 extends BaseEntity {

    private String medicalInstitution;
    private String documentSentDate;
    private String documentSerialNumber;
    private String barcodeFill;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String livingPlace;
    private String familyStatus;
    private String socialProfessionalStatus;
    private String populationCategory;
    private String riskGroup;
    private LocalDate lastFluorographyDate;
    private String diseaseDetectionLocation;
    private LocalDate firstMedicalVisitDate;
    private LocalDate tuberculosisRegistrationDate;
    private String diseaseDetectionMethods;
    private String detectedInMonitoredGroups;
    private String detectionMethod;
    private String diagnosis;
    private String presenceOfDecay;
    private String confirmationMethod;
    private String associatedDiseases;
    private boolean registeredInNarcologicalDispensary;
    private LocalDate tuberculosisDiagnosisConfirmationDate;
    private LocalDate notificationCompletionDate;

}
