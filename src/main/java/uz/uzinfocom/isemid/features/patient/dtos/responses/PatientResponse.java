package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String middleName;
    private List<IdentifierResponse> identifiers;
    private LocalDate birthDate;
    private Integer ageYears;
    private Integer ageMonths;
    private CatalogResponseDto gender;
    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;
    private CatalogResponseDto residentialStatus;
    private CatalogResponseDto maritalStatus;
    private CatalogResponseDto populationType;
    private CatalogResponseDto category;
    private CatalogResponseDto profession;
    private List<AffiliationResponse> affiliations;
    private List<AddressResponse> addresses;
}
