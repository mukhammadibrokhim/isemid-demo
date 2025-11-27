package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Entity;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form3_1Report extends AbsEntity {
    // 2. ГПЗ / Grippga o'xshash kasallik
    private Integer influenzaLikeIllness;

    // 3. ОРИ / URI
    private Integer acuteRespiratoryInfection;

    // 4. ОП / O'tkir pankreatit
    private Integer acutePancreatitis;

    // 5. ТОРИ / OURI – jami
    private Integer severeAcuteRespiratoryInfectionTotal;

    // 6. ТОРИ / OURI – ulardan homiladorlar
    private Integer severeAcuteRespiratoryInfectionPregnant;

    // 7. Умершие / O‘lganlar – jami
    private Integer deathTotal;

    // 8. O‘lganlar – ulardan homiladorlar
    private Integer deathPregnant;

    // 9. Vaktsinatsiya grippga qarshi – hafta davomida
    private Integer vaccinationWeekly;

    // 10. Vaktsinatsiya grippga qarshi – davr boshidan
    private Integer vaccinationFromStart;
}
