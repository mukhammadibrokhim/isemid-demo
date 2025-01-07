package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Имя обязательно и не может быть пустым.")
    private String firstName;
    @NotBlank(message = "Фамилия обязательна и не может быть пустой.")
    private String lastName;
    private String middleName;
    @Valid
    @NotNull(message = "Список идентификаторов обязателен.")
    @Size(min = 1, message = "Список идентификаторов не может быть пустым.")
    private List<ReqIdentifier> identifiers;

    @Pattern(regexp = "^\\(\\d{2}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Проверьте формат телефонного номера: (00)000-00-00")
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
