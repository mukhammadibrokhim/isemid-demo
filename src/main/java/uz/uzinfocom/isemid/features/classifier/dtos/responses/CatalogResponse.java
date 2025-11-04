package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String type;
    private String code;
    private String parentCode;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
}
