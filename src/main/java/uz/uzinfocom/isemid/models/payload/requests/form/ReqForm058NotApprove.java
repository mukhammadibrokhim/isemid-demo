package uz.uzinfocom.isemid.models.payload.requests.form;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReqForm058NotApprove {
    @Size(max = 1000)
    String comment;
}
