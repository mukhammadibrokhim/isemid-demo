package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_tb_history")
public class TBHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private String location; // Infeksiya joyi (organ yoki tana qismi)
    private LocalDate date; // Infeksiya aniqlangan sana
    private String mkbCode; // MKB-10 kodi
    private String mkbName; // MKB-10 nomi
    private String regGroup; // Ro‘yxatga olish guruhi
}
