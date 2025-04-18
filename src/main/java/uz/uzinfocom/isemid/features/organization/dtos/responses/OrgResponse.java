package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.Builder;
import lombok.Data;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;

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
    private CatalogRespDto level;
    private CatalogRespDto country;
    private CatalogRespDto state;
    private CatalogRespDto city;
    private String district;
    private String line;
    private CatalogRespDto serviceArea;
    private Long parentId;
    private List<OrgChildResponse> children;
}
