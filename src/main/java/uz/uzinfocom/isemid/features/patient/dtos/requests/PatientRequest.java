package uz.uzinfocom.isemid.features.patient.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.patient.enums.MaritalStatus;
import uz.uzinfocom.isemid.features.patient.enums.PopulationType;
import uz.uzinfocom.isemid.features.patient.enums.ResidentialStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientRequest {

    @NotBlank(message = "Имя обязательно и не может быть пустым.")
    private String firstName;
    @NotBlank(message = "Фамилия обязательна и не может быть пустой.")
    private String lastName;
    private String middleName;
    @Valid
    @NotEmpty(message = "Список идентификаторов обязателен.")
    @Size(min = 1, message = "Список идентификаторов не может быть пустым.")
    private List<IdentifierRequest> identifiers;

    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;
    private LocalDate birthDate;
    private Integer ageYears;
    private Integer ageMonths;
    private Gender gender;

    @Valid
    @NotEmpty(message = "Список адресов не может быть пустым.")
    private List<AddressRequest> addresses;

    private ResidentialStatus residentialStatus;
    private MaritalStatus maritalStatus;

    private String categoryCode;
    private String professionCode;

    @NotNull(message = "Population Type is required.")
    private PopulationType populationType;

    @NotBlank(message = "Место работы обязательно и не может быть пустым.")
    private String workPlace;
    private String workPlaceStateCode;
    private String workPlaceCityCode;

    @JsonIgnore
    @AssertTrue(message = "Адреса должны содержать как минимум один TEMPORARY и один PERMANENT адрес.")
    public boolean isValidAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return false;
        }
        boolean hasTemporary = addresses.stream().anyMatch(a -> AddressType.TEMPORARY.equals(a.getType()));
        boolean hasPermanent = addresses.stream().anyMatch(a -> AddressType.PERMANENT.equals(a.getType()));
        return hasTemporary && hasPermanent;
    }
}
