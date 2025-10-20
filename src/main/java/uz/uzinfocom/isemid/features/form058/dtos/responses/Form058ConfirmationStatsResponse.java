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
    private String regionName;
    private List<Form058Mkb10Stat> mkb10Stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Form058Mkb10Stat {
        private String code;
        private String name;
        private Long confirmed;
        private Long unconfirmed;
    }
}