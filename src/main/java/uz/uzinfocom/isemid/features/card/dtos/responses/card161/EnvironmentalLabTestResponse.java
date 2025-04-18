package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvironmentalLabTestResponse {
    private Long id;
    private LocalDate examinationDate;
    private String objectArthropodsAnimals;
    private String material;
    private String sampleQuantity;
    private String testTypeAndResult;}
