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

    @NotBlank(message = "Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10;

    @NotBlank(message = "Название Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Name;

    @NotBlank(message = "Медицинское учреждение обязательно и не может быть пустым.")
    private String medicalInstitution;

    @NotBlank(message = "Дата отправки документа обязательна и не может быть пустой.")
    private String documentSentDate;

    @NotBlank(message = "Серийный номер документа обязателен и не может быть пустым.")
    private String documentSerialNumber;

    @NotBlank(message = "Заполнение штрихкода обязательно и не может быть пустым.")
    private String barcodeFill;

    @Valid
    private ReqPatient patient;

    @NotBlank(message = "Место жительства обязательно и не может быть пустым.")
    private String livingPlace;

    @NotBlank(message = "Семейное положение обязательно и не может быть пустым.")
    private String familyStatus;

    @NotBlank(message = "Социально-профессиональный статус обязателен и не может быть пустым.")
    private String socialProfessionalStatus;

    @NotBlank(message = "Категория населения обязательна и не может быть пустой.")
    private String populationCategory;

    @NotBlank(message = "Группа риска обязательна и не может быть пустой.")
    private String riskGroup;

    @NotNull(message = "Дата последней флюорографии обязательна.")
    private LocalDate lastFluorographyDate;

    @NotBlank(message = "Место выявления заболевания обязательно и не может быть пустым.")
    private String diseaseDetectionLocation;

    @NotNull(message = "Дата первого медицинского визита обязательна.")
    private LocalDate firstMedicalVisitDate;

    @NotNull(message = "Дата постановки на учет по туберкулезу обязательна.")
    private LocalDate tuberculosisRegistrationDate;

    @NotBlank(message = "Методы выявления заболевания обязательны и не могут быть пустыми.")
    private String diseaseDetectionMethods;

    @NotBlank(message = "Выявлено в наблюдаемых группах обязательно и не может быть пустым.")
    private String detectedInMonitoredGroups;

    @NotBlank(message = "Метод выявления заболевания обязателен и не может быть пустым.")
    private String detectionMethod;

    @NotBlank(message = "Диагноз обязателен и не может быть пустым.")
    private String diagnosis;

    @NotBlank(message = "Наличие разрушения обязательно и не может быть пустым.")
    private String presenceOfDecay;

    @NotBlank(message = "Метод подтверждения обязателен и не может быть пустым.")
    private String confirmationMethod;

    @NotBlank(message = "Ассоциированные заболевания обязательны и не могут быть пустыми.")
    private String associatedDiseases;

    private boolean registeredInNarcologicalDispensary;

    @NotNull(message = "Дата подтверждения диагноза туберкулеза обязательна.")
    private LocalDate tuberculosisDiagnosisConfirmationDate;

    @NotNull(message = "Дата завершения уведомления обязательна.")
    private LocalDate notificationCompletionDate;
}
