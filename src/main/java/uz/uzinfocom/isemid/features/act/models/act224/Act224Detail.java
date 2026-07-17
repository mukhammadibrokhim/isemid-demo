package uz.uzinfocom.isemid.features.act.models.act224;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "act224_detail")
public class Act224Detail extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String recommendedActivities;

    private LocalDateTime executionPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act224_id")
    @JsonBackReference
    private Act224 act224;
}
