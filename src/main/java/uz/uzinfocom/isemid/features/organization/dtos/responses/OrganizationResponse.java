package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationResponse {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private UUID uuid;
    private String name;
    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private CatalogMinResponse medicalType;
    private CatalogMinResponse levelType;
    private List<CatalogMinResponse> serviceTypes;
    private CatalogMinResponse country;
    private CatalogMinResponse state;
    private CatalogMinResponse city;
    private String district;
    private String line;
    private CatalogMinResponse serviceArea;
    private Long parentId;
    private List<OrgChildResponse> children;
}
