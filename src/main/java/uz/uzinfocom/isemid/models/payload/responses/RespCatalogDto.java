package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespCatalogDto {
    private Long id;
    private String code;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
}
