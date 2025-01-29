package uz.uzinfocom.isemid.models.payload.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.enums.*;

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
    @NotEmpty(message = "Список идентификаторов обязателен.")
    @Size(min = 1, message = "Список идентификаторов не может быть пустым.")
    private List<ReqIdentifier> identifiers;

//    @Pattern(regexp = "^\\(\\d{2}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Проверьте формат телефонного номера: (00)000-00-00")
    private String phoneNumber;

    private LocalDate birthDate;
    private Gender gender;
    @Valid
    @NotEmpty(message = "Список адресов не может быть пустым.")
    private List<ReqAddress> addresses;
    private ResidentialStatus residentialStatus;
    private MaritalStatus maritalStatus;

    @NotNull(message = "Population Type is required.")
    private PopulationType populationType;

    @NotBlank(message = "Место работы обязательно и не может быть пустым.")
    private String workPlace;

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
