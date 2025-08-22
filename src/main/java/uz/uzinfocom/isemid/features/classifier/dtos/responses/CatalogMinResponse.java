package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CatalogMinResponse {
    private Long id;
    private String code;
    private String parentCode;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
}
