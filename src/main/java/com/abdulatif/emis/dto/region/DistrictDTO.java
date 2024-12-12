package com.abdulatif.emis.dto.region;

import com.abdulatif.emis.model.region.District;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictDTO {

    private Long id;
    private String name;
    private String region;

    public DistrictDTO(District district) {
        setId(district.getId());
        setName(district.getName());
        setRegion(district.getRegion().getName());
    }


}
