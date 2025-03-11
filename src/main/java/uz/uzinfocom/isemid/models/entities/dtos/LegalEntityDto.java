package uz.uzinfocom.isemid.models.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LegalEntityDto {
    private Company company;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Company {
        private String name;
        private String shortName;
        private String tin;
    }
}
