package uz.uzinfocom.isemid.models.payload.requests.act;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqCardAssignAct {
    @NotNull(message = "Card id is required.")
    private long cardId;
    @NotNull(message = "Act type is required.")
    private List<ActType> actTypes;
}
