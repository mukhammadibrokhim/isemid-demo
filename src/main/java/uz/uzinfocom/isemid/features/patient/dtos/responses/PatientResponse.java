package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

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

    private CatalogRespDto gender;
    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;
    private CatalogRespDto residentialStatus;
    private CatalogRespDto maritalStatus;
    private CatalogRespDto populationType;
    private CatalogRespDto category;
    private CatalogRespDto profession;
    private String workPlace;
    private CatalogRespDto workPlaceState;
    private CatalogRespDto workPlaceCity;
    private OrgMiniResponse workPlaceOrg;
    private List<AddressResponse> addresses;
}
