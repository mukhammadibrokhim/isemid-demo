package uz.uzinfocom.isemid.features.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PermissionResponse {
    private Long id;
    private String subject;
    private boolean active;
    private List<String> actions;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionUzCyril;
    private String descriptionKaa;
}
