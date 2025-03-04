package uz.uzinfocom.isemid.models.payload.requests.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqRejectCard {
    @Size(max = 1000)
    @NotBlank
    private String comment;
}
