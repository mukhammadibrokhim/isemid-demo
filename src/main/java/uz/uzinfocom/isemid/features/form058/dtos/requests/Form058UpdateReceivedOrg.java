package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058UpdateReceivedOrg {
    @NotBlank(message = "{validation.form058.receivedOrgId.notNull}")
    private Long receivedOrgId;
}
