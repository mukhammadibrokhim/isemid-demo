package uz.uzinfocom.isemid.features.organization.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrgChildResponse {
    private Long id;
    private UUID uuid;
    private String name;
    private Long parentId;
    private List<OrgChildResponse> children;
}
