package uz.uzinfocom.isemid.features.user.dtos.requests;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
