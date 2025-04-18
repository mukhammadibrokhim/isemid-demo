package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class RoleResponse {
    private String name;
    private boolean active;
    private String descriptionUz;
    private String descriptionUzCyril;
    private String descriptionRu;
    private String descriptionKaa;
    private Set<PermissionResponse> permissions;
}
