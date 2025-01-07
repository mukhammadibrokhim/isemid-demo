package uz.uzinfocom.isemid.models.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespCatalog {
    private Long id;
    private String parentCode;
    private String code;
    private String display;
}
