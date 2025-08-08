package uz.uzinfocom.isemid.features.card.dtos.requests.card_tube;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfectionSourceRequest {
    private Long id;
    private String tbContactCode; // Sil bilan kasallangan bemor bilan aloqa qilish (spravochnik)
    private String fullName; // Manbaning familiyasi va ismi
    private String relationDegreeCode; // Qarindoshlik darajasi (spravochnik)
    private String contactDuration; // Bemor bilan muloqat vaqti va davomiyligi
}
