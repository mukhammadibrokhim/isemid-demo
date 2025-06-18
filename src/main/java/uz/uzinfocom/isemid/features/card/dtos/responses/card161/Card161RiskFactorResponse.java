package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponseDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card161RiskFactorResponse {
    private Long id;
    private CatalogResponseDto riskFactor;
    private String addressLocation;
    private String seasonTime;}
