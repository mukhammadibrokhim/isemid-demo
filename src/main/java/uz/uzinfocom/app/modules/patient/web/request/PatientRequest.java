package uz.uzinfocom.app.modules.patient.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Сведения о пациенте, регистрируемом вместе с формой №058.")
public record PatientRequest(

        @Schema(description = "Имя пациента.")
        @Size(max = 255, message = "{patient.request.first_name.size}")
        String firstName,

        @Schema(description = "Фамилия пациента.")
        @Size(max = 255, message = "{patient.request.last_name.size}")
        String lastName,

        @Schema(description = "Отчество пациента.")
        @Size(max = 255, message = "{patient.request.middle_name.size}")
        String middleName,

        @Schema(description = "Дата рождения пациента.")
        @PastOrPresent(message = "{patient.request.birth_date.past_or_present}")
        LocalDate birthDate,

        @Schema(description = "Код пола пациента (по справочнику).")
        @Size(max = 64, message = "{patient.request.gender_code.size}")
        String genderCode,

        @Schema(description = "Контактный номер телефона пациента.")
        @Size(max = 32, message = "{patient.request.phone_number.size}")
        String phoneNumber,

        @Schema(description = "Код степени родства с контактным лицом (если сведения предоставлены родственником).")
        @Size(max = 64, message = "{patient.request.kinship_degree.size}")
        String kinshipDegree,

        @Schema(description = "ФИО родственника/контактного лица.")
        @Size(max = 500, message = "{patient.request.kinship_full_name.size}")
        String kinshipFullName,

        @Schema(description = "Код статуса проживания пациента (по справочнику).")
        @Size(max = 64, message = "{patient.request.residential_status_code.size}")
        String residentialStatusCode,

        @Schema(description = "Код семейного положения пациента (по справочнику).")
        @Size(max = 64, message = "{patient.request.marital_status_code.size}")
        String maritalStatusCode,

        @Schema(description = "Код типа населения (городское/сельское, по справочнику).")
        @Size(max = 64, message = "{patient.request.population_type_code.size}")
        String populationTypeCode,

        @Schema(description = "Код категории пациента (по справочнику).")
        @Size(max = 64, message = "{patient.request.category_code.size}")
        String categoryCode,

        @Schema(description = "Код профессии пациента (по справочнику).")
        @Size(max = 64, message = "{patient.request.profession_code.size}")
        String professionCode,

        @Schema(description = "Список документов, удостоверяющих личность пациента.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotEmpty(message = "{patient.request.identifiers.required}")
        List<CreatePatientIdentifierRequest> identifiers,

        @Schema(description = "Список адресов пациента.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotEmpty(message = "{patient.request.addresses.required}")
        List<CreatePatientAddressRequest> addresses,

        @Schema(description = "Список принадлежностей пациента (место работы/учёбы).")
        @Valid
        List<CreatePatientAffiliationRequest> affiliations

) {

    public PatientRequest {
        identifiers = immutableCopy(identifiers);
        addresses = immutableCopy(addresses);
        affiliations = immutableCopy(affiliations);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
