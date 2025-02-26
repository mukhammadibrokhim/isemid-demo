package uz.uzinfocom.isemid.models.payload.requests.card.card_tube;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqInfectionSource {
    private String tbContactCode; // Sil bilan kasallangan bemor bilan aloqa qilish (spravochnik)
    private String fullName; // Manbaning familiyasi va ismi
    private String relationDegreeCode; // Qarindoshlik darajasi (spravochnik)
    private String contactDuration; // Bemor bilan muloqat vaqti va davomiyligi
}
