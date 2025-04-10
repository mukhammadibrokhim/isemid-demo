package uz.uzinfocom.isemid.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form1ReportDTO {
    @JsonIgnoreProperties({"createdAt","updatedAt","type","id","uuid", "createdBy", "updatedBy"})
    private Catalog name; // Hudud nomi (Respublika, viloyat, tuman)
    private String code; // Hudud kodi
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
