package uz.uzinfocom.isemid.features.user.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PermissionRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private List<String> actions;
    private Boolean active;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionUzCyril;
    private String descriptionKaa;

}
