package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomePreventiveMeasureResponse {
    private Long id;
    private String notifiedPerson;
    private LocalDateTime nextDiseaseTime; //DMED integration
    private String drugName; //DMED integration
    private String dose; //DMED integration
    private String series; //DMED integration
    private LocalDateTime resultTime; //integration LIS
    private LocalDate receivedDate; // Олинган сана
    private String lisResult;//integration LIS
    private String observationResult;}
