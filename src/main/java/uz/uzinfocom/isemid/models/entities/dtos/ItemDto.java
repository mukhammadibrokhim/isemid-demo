package uz.uzinfocom.isemid.models.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private String id;
    private String parentId;
    private String code;
    private String parentCode;
    private String nameUz;
    private String nameEn;
    private String level;
}
