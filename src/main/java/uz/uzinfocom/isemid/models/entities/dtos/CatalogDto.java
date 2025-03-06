package uz.uzinfocom.isemid.models.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogDto {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private long createdBy;
    private long updatedBy;
    private String type;
    private String code;
    private String nameRu;
    private String nameUz;
    private String nameUzCyril;
    private String nameKaa;
    private String parentCode;
    private CatalogDto parent;
}
