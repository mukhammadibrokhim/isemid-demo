package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMeResponse {

    private Long id;
    private UUID uuid;
    private String nnuzb;
    private String ppn;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fullName;
    private LocalDate birthDate;
    private boolean active;
    private CatalogRespDto gender;
    private String phoneNumber;
    private CatalogRespDto position;
    private CatalogRespDto country;
    private CatalogRespDto state;
    private CatalogRespDto city;
    private String district;
    private String line;
    private List<OrgMiniResponse> organizations;
    private OrgMiniResponse currentOrganization;
    private Set<RoleResponse> roles;
}
