package uz.uzinfocom.isemid.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form1ReportDTO {
    @JsonIgnoreProperties({"createdAt","updatedAt","type","id","uuid", "createdBy", "updatedBy"})
    private String name; // Hudud nomi (Respublika, viloyat, tuman)
    private String level; // "REPUBLIC", "REGION", "DISTRICT"

    private int confirmedTotal;
    private int confirmedUnder18;
    private int confirmedAdults;
    private int confirmedFemales;

    private int emergencyTotal;
    private int emergencyUnder18;
    private int emergencyAdults;
    private int emergencyFemales;

    private double growthRate;

    private Set<Form1ReportDTO> children = new HashSet<>();
}
