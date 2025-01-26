package uz.uzinfocom.isemid.models.payload.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.card.Card;

import java.time.LocalDateTime;

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

    @NotBlank(message = "Место госпитализации обязательно и не может быть пустым.")
    private String hospitalPlace;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата поступления обязательна.")
    private LocalDateTime admissionDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата заболевания обязательна.")
    private LocalDateTime diseaseDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата первого визита обязательна.")
    private LocalDateTime firstVisitDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата постановки диагноза обязательна.")
    private LocalDateTime diagnosisDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата визита обязательна.")
    private LocalDateTime visitDate;

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
    @NotBlank(message = "Форма журнала обязательна и не пуста.")
    private String journalForm;
    private String deceaseLocation;
    private String comment;
    private Card card;
}
