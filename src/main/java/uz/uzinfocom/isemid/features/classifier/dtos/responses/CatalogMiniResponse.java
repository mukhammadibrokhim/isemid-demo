package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogMiniResponse {
    private Long id;
    private String code;
    private String parentCode;
    private String display;
}
