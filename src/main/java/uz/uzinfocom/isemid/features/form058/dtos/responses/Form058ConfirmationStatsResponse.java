package uz.uzinfocom.isemid.features.form058.dtos.responses;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form058ConfirmationStatsResponse {
    private String regionCode;
    private List<String> mkb10Codes;
    private Long confirmed;
    private Long unconfirmed;
}