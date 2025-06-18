package uz.uzinfocom.isemid.features.integrations.info.dtos.responses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfoIcd10Response {
    private List<String> mkb10s;
}
