package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.Builder;
import lombok.Data;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrgResponse {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private UUID uuid;
    private String name;
    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private String type;
    private CatalogMinResponse level;
    private CatalogMinResponse country;
    private CatalogMinResponse state;
    private CatalogMinResponse city;
//    private CatalogResponseDto ;
    private String district;
    private String line;
    private CatalogMinResponse serviceArea;
    private Long parentId;
    private List<OrgChildResponse> children;
}
