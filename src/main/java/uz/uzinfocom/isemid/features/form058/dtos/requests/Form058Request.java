package uz.uzinfocom.isemid.features.form058.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.patient.dtos.requests.PatientRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058Request {

    @NotBlank(message = "Код МКБ-10 не должен быть пустым.")
    private String mkb10Code;

    @NotBlank(message = "Название Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Name;
    private Integer mkb10UsageLimit;

    @NotBlank(message = "Итоговый код МКБ-10 не должен быть пустым.")
    private String finalMkb10Code;

    @NotBlank(message = "Итоговое название МКБ-10 не должно быть пустым.")
    private String finalMkb10Name;

    @Valid
    @NotNull(message = "Объект пациента обязателен.")
    private PatientRequest patient;

    private Boolean labConfirmation;

    @NotNull(message = "Место госпитализации обязательно и не может быть пустым.")
    private Long hospitalPlaceId;

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
//    @NotNull(message = "Дата постановки диагноза обязательна.")
    private LocalDateTime diagnosisDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата визита обязательна.")
    private LocalDateTime visitDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime docSendDate; // document jo'natilgan sana


    @NotNull(message = "Организация-отправитель обязательна и не может быть пустой.")
    private UUID senderOrganizationId;

    @NotNull(message = "Организация-получатель обязательна и не может быть пустой.")
    private Long receiverOrganizationId;

    private LocationRequest location;

    @NotNull
    private String diseasePlaceCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата обязательна.")
    private LocalDateTime initialReportDateTime;

    @NotBlank(message = "Причина заболевания обязательна и не может быть пустой.")
    private String diseaseCause;

    @NotBlank(message = "Эпидемические меры обязательны и не могут быть пустыми.")
    private String epidemicMeasures;

    @NotBlank(message = "Ф.И.О. уведомляющего лица не может быть пустым.")
    private String notifierFullName;

    @NotBlank(message = "Форма журнала обязательна и не пуста.")
    private String journalFormCode;

    private String comment;
}
