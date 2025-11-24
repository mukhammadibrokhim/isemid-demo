package uz.uzinfocom.isemid.features.report.dtos;


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
public class Form1ReportDto {

    private String name; // Hudud nomi (Respublika, viloyat, tuman)
    private long confirmedTotal;
    private long confirmedUnder14;
    private long confirmedUnder18;
    private long confirmedAdults;
    private long confirmedFemales;

    private long emergencyTotal;
    private long emergencyUnder14;
    private long emergencyUnder18;
    private long emergencyAdults;
    private long emergencyFemales;

    private double growthRate;
    @Builder.Default
    private Set<Form1ReportDto> children = new HashSet<>();

}
