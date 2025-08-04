package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogDisplayedResponse {
    private Long id;
    private String code;
    private String parentCode;
    private String display;
}
