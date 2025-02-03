package uz.uzinfocom.isemid.models.payload.requests;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqCatalog {

    @NotBlank(message = "Тип не должен быть пустым или null.")
    private String type;

    @NotBlank(message = "Код не должен быть пустым или null.")
    private String code;

    private String parentCode;

    @NotBlank(message = "Название на русском не должно быть пустым или null.")
    @Size(max = 1000, message = "Название на русском не должно превышать 1000 символов.")
    private String nameRu;

    @NotBlank(message = "Название на узбекском не должно быть пустым или null.")
    @Size(max = 1000, message = "Название на узбекском не должно превышать 1000 символов.")
    private String nameUz;

    @NotBlank(message = "Название на узбекском (кириллицей) не должно быть пустым или null.")
    @Size(max = 1000, message = "Название на узбекском (кириллицей) не должно превышать 1000 символов.")
    private String nameUzCyril;

    @NotBlank(message = "Название на каракалпакском языке не должно быть пустым или null.")
    @Size(max = 1000, message = "Название на каракалпакском языке не должно превышать 1000 символов.")
    private String nameKaa;
}
