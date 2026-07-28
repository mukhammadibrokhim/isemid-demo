package uz.uzinfocom.isemid.features.report.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form8ReportDto {
    private String name; // Hudud nomi (Respublika, viloyat, tuman)
    private long unorganized;
    private long organized;
    private long pupils;
    private long secondaryInstitutions;
    private long institutions;
    private long employee;
    private long medEmployee;
    private long unEmployee;
    private long pensioners;
    private long homelessPeople;
    private long staff;
    private long total;
}
