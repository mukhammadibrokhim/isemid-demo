package uz.uzinfocom.isemid.models.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ReportDto {
    private String orgName; // Muassasa nomi
    private String orgState;
    private String stateCode;
    private String orgCity;
    private String level;
    private LocalDate reportDate; // Hisobot sanasi
    private LocalDate dateFrom; // Hisobot davri boshlanish sanasi
    private LocalDate dateTo; // Hisobot davri tugash sanasi
    private int confirmedTotal; // Tasdiqlangan 058 shakllari soni
    private int confirmedAge0To17; // 0-17 yosh
    private int confirmedAge18Plus; // 18 yoshdan kattalar
    private int confirmedFemaleCount; // Ayollar soni

    private int total; // Tasdiqlangan 058 shakllari soni
    private int age0To17; // 0-17 yosh
    private int age18Plus; // 18 yoshdan kattalar
    private int femaleCount; // Ayollar soni
//    @Builder.Default
//    private List<Form058ReportDto> children = new ArrayList<>(); // Hududning ichki bo‘linmalari (Viloyat → Tumanlar)
}
