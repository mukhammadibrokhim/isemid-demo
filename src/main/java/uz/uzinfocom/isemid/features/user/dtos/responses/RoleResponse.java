package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RoleResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String name;
    private boolean active;
    private String descriptionUz;
    private String descriptionUzCyril;
    private String descriptionRu;
    private String descriptionKaa;
    private Set<PermissionResponse> permissions;
}
