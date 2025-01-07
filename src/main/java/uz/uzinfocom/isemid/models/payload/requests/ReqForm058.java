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

    @NotBlank(message = "Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Code;

    @NotBlank(message = "Название Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Name;

    @Valid
    @NotNull(message = "Объект пациента обязателен.")
    private ReqPatient patient;

    @NotBlank(message = "Место работы обязательно и не может быть пустым.")
    private String workPlace;

    @NotBlank(message = "Место госпитализации обязательно и не может быть пустым.")
    private String hospitalPlace;

    @NotNull(message = "Дата поступления обязательна.")
    private LocalDate admissionDate;

    @NotNull(message = "Дата заболевания обязательна.")
    private LocalDate diseaseDate;

    @NotNull(message = "Дата первого визита обязательна.")
    private LocalDate firstVisitDate;

    @NotNull(message = "Дата постановки диагноза обязательна.")
    private LocalDate diagnosisDate;

    @NotNull(message = "Дата визита обязательна.")
    private LocalDate visitDate;

    @NotBlank(message = "Организация-отправитель обязательна и не может быть пустой.")
    private String senderOrganization;

    @NotBlank(message = "Организация-получатель обязательна и не может быть пустой.")
    private String receiverOrganization;

    @NotBlank(message = "Место отравления обязательно и не может быть пустым.")
    private String poisoningLocation;

    @NotBlank(message = "Причина заболевания обязательна и не может быть пустой.")
    private String diseaseCause;

    @NotBlank(message = "Эпидемические меры обязательны и не могут быть пустыми.")
    private String epidemicMeasures;

    @NotBlank(message = "Ф.И.О. уведомляющего лица не может быть пустым.")
    private String notifierFullName;

    private String comment;
}
