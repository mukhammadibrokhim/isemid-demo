package uz.uzinfocom.isemid.models.payload.requests.card;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqCardAssignUsers {

    @NotNull
    private Long cardId;

    @Valid
    @NotEmpty
    private List<Long> userIds;
}
