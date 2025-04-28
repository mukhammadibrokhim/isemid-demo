package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CatalogResponseDto {
    private Long id;
    private String code;
    private String parentCode;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
}
