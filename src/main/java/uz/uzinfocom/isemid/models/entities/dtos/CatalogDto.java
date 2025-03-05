package uz.uzinfocom.isemid.models.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogDto {
    private Long id;
    private UUID uuid;
    private String code;
    private String nameUz;
    private String nameRu;
    private String parentCode;
    private CatalogDto parent;
}
