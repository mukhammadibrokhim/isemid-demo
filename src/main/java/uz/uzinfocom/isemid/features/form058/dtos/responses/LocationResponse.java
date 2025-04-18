package uz.uzinfocom.isemid.features.form058.dtos.responses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationResponse {
    private Double latitude;
    private Double longitude;
    private String location;
}
