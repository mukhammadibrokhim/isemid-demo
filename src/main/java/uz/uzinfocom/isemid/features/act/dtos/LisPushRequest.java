package uz.uzinfocom.isemid.features.act.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LisPushRequest {
    private String isemidData;
    private String redirectUrl;
}
