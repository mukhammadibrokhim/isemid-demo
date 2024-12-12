package com.abdulatif.emis.dto.region;

import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.region.Region;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionDTO {
    private Long id;
    private String name;
    private List<District> districts;

    public RegionDTO(Region region) {
        setId(region.getId());
        setName(region.getName());
    }
}
