package uz.uzinfocom.isemid.models.payload.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqPermission {

    @NotBlank(message = "Name is required and not blank.")
    private String name;
    @NotBlank(message = "Name is required and not blank.")
    private String descriptionUz;
    @NotBlank(message = "descriptionUz is required and not blank.")
    private String descriptionRu;
    @NotBlank(message = "descriptionUzCyril is required and not blank.")
    private String descriptionUzCyril;
    @NotBlank(message = "descriptionKaaUz is required and not blank.")
    private String descriptionKaa;
}
