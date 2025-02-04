package uz.uzinfocom.isemid.models.payload.requests.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.payload.requests.ReqLocation;
import uz.uzinfocom.isemid.models.payload.requests.ReqPatient;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqForm058 {

    @NotBlank(message = "Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Code;

    @NotBlank(message = "Название Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Name;

    @Valid
    @NotNull(message = "Объект пациента обязателен.")
    private ReqPatient patient;

    @NotNull
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
    @NotNull(message = "Дата постановки диагноза обязательна.")
    private LocalDateTime diagnosisDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата визита обязательна.")
    private LocalDateTime visitDate;

    @NotNull(message = "Организация-отправитель обязательна и не может быть пустой.")
    private Long senderOrganizationId;

    @NotNull(message = "Организация-получатель обязательна и не может быть пустой.")
    private Long receiverOrganizationId;

    @Valid
    @NotNull
    private ReqLocation location;

    @NotNull
    private String diseasePlaceCode;

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
