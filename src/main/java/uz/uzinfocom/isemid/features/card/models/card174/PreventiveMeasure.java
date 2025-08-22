package uz.uzinfocom.isemid.features.card.models.card174;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card174_preventive_measure")
public class PreventiveMeasure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "card174_id", nullable = false)
    private Card174 card174;

    private String measures;           //chora tadbirlar
    private LocalDate measureDate;     // Chora sanasi
    private Integer measureCount;      // Choralar soni
    private String measureLocation;    // O‘tkazilgan joy
}
