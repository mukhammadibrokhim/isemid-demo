package uz.uzinfocom.isemid.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mkb10Dto {
    private Long id;
    private Long secondary_id;
    private Long parent_id;
    private String code;
    private int level;
    private boolean is_last_level;
    private String name_uz;
    private String name_en;
    private String name_ru;
    private String name_uz_cyrl;
}
