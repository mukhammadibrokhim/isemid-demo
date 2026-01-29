package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientMiniResponse {
    private Long id;
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String middleName;
    @Builder.Default
    private List<IdentifierResponse> identifiers = new ArrayList<>();
    private LocalDate birthDate;
    private Integer ageYears;
    private Integer ageMonths;
    private CatalogMinResponse gender;
    private String phoneNumber;
    @Builder.Default
    private List<AddressResponse> addresses = new ArrayList<>();
}
