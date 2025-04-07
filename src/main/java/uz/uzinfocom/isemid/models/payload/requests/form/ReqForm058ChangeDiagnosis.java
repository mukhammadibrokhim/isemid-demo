package uz.uzinfocom.isemid.models.payload.requests.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqForm058ChangeDiagnosis {
    private String finalMkb10Code;
    private String finalMkb10name;
}
