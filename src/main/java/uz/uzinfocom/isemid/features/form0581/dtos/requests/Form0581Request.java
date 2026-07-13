package uz.uzinfocom.isemid.features.form0581.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.patient.dtos.requests.PatientRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581Request {

    @NotBlank(message = "Код МКБ-10 не должен быть пустым.")
    private String mkb10Code;

    @NotBlank(message = "Название Mkb10 не может быть пустым и обязательно для заполнения.")
    private String mkb10Name;
    private Integer mkb10UsageLimit;

    private String finalMkb10Code;
    private String finalMkb10Name;

    @NotBlank(message = "Jarohat olgan lokalizatsiya bo'sh bo'lishi mumkin emas.")
    private String injuryLocalization;

    private String medRecordId;

    @Valid
    @NotNull(message = "Объект пациента обязателен.")
    private PatientRequest patient;

    @NotNull(message = "Организация-отправитель обязательна и не может быть пустой.")
    private UUID senderOrganizationId;

    @NotNull(message = "Организация-получатель обязательна и не может быть пустой.")
    private UUID receiverOrganizationId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Jarohatlangan yoki so'laklangan sana majburiy.")
    private LocalDateTime injuryDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "DPMga murojaat qilgan sana majburiy.")
    private LocalDateTime visitDateTime;

    @NotBlank(message = "Jarohat olgan manzil viloyati majburiy.")
    private String injuryStateCode;

    @NotBlank(message = "Jarohat olgan manzil tumani/shahri majburiy.")
    private String injuryCityCode;

    private String injuryAddress;

    @Valid
    private AnimalOwnerRequest animalOwner;

    @NotBlank(message = "Hayvon kategoriyasi majburiy.")
    private String animalCategoryCode;

    private String animalColor;
    private String animalType;
    private String animalBreed;

    @NotNull(message = "Boshqa odamlar jarohat olgan-olmaganligi ko'rsatilishi shart.")
    private Boolean otherPeopleBitten;

    @Valid
    @Builder.Default
    private List<BittenPersonRequest> bittenPersons = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime hospitalizedAt;

    private UUID hospitalPlaceId;

    private String antirabicAidInfo;

    @NotBlank(message = "Ф.И.О. уведомляющего лица не может быть пустым.")
    private String notifierFullName;

    private String receiverFullName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Дата обязательна.")
    private LocalDateTime initialReportDateTime;
}
