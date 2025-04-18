package uz.uzinfocom.isemid.features.user.dtos.requests;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleRequest {
    private String name;
    private Boolean isActive;
    private String descriptionUz;
    private String descriptionUzCyril;
    private String descriptionRu;
    private String descriptionKaa;
    private Set<Long> permissionIds;
}
