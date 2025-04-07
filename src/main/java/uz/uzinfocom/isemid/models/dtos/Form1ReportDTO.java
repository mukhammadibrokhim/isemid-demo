package uz.uzinfocom.isemid.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form1ReportDTO {

    private String name;          // Organization name
    private String code;          // Organization code
    private String stateName;     // State name
    private String stateCode;     // State code
    private String cityName;      // City name
    private String cityCode;      // City code
    private String level;

    private int confirmedTotal;
    private int confirmedChildren;
    private int confirmedAdults;
    private int confirmedFemales;

    private double ratio;

    @Builder.Default
    private List<Form1ReportDTO> children = new ArrayList<>();
}
