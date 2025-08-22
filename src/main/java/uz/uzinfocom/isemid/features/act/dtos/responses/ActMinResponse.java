package uz.uzinfocom.isemid.features.act.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActMinResponse {
    private Long id;
    private UUID uuid;
    private ActStatus status;
    private ActType actType;
}
