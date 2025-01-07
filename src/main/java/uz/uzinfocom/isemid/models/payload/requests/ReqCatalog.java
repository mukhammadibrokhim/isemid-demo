package uz.uzinfocom.isemid.models.payload.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqCatalog {

    @NotBlank(message = "Type must not be null or empty.")
    private String type;
    @NotBlank(message = "Code must not be null or empty.")
    private String code;
    private String parentCode;
    @NotBlank(message = "Name ru must not be null or empty.")
    @Size(max = 1000, message = "NameRu must not exceed 1000 characters.")
    private String nameRu;
    @NotBlank(message = "Name uz must not be null or empty.")
    @Size(max = 1000, message = "NameUz must not exceed 1000 characters.")
    private String nameUz;
    @NotBlank(message = "Name uz cyril must not be null or empty.")
    @Size(max = 1000, message = "NameUzCyril must not exceed 1000 characters.")
    private String nameUzCyril;
    @NotBlank(message = "Name kaa cyril must not be null or empty.")
    @Size(max = 1000, message = "NameKaa must not exceed 1000 characters.")
    private String nameKaa;
}
