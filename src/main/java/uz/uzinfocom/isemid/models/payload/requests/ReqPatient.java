package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.entities.enums.MaritalStatus;
import uz.uzinfocom.isemid.models.entities.enums.ResidentialStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqPatient {
    @NotBlank(message = "First name is required and not blank.")
    private String firstName;
    @NotBlank(message = "Last name is required and not blank.")
    private String lastName;
    private String middleName;
    @Valid
    @NotNull(message = "Identifiers list is required.")
    @Size(min = 1, message = "Identifiers list cannot be empty.")
    private List<ReqIdentifier> identifiers;

    private String phoneNumber;

    private LocalDate birthDate;
    private Gender gender;
    private String stateCode;
    private String cityCode;
    private String neighborhood;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber; // Xonadon
    private ResidentialStatus residentialStatus; // Yashash joyi holati
    private MaritalStatus maritalStatus; // Oila sharoiti
    private String workPlace; // Ish joyi va manzili

}
