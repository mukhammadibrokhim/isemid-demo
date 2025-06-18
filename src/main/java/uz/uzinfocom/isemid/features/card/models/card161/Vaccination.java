package uz.uzinfocom.isemid.features.card.models.card161;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Vaccination extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;

    private String vaccinationVerifiedCode;

    private String vaccinationName;   // Preparat nomi
    private String serialNumber;      // Seriya raqami
    private LocalDateTime vaccinationDate; // Vaqti (GMT+5 Toshkent)
    private int doseVolume;           // Doza hajmi
    private boolean scheduled;        // Rejeli emlashmi?
}
