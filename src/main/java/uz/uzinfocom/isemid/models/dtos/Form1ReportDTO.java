package uz.uzinfocom.isemid.models.dtos;

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

    private String name; // Hudud nomi (Respublika, viloyat, tuman)
    private long confirmedTotal;
    private long confirmedUnder18;
    private long confirmedAdults;
    private long confirmedFemales;

    private long emergencyTotal;
    private long emergencyUnder18;
    private long emergencyAdults;
    private long emergencyFemales;

    private double growthRate;

    private Set<Form1ReportDTO> children = new HashSet<>();
}
