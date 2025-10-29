package uz.uzinfocom.isemid.features.analytic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "analytic_report")
public class AnalyticReport extends AbsEntity {
    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String payload;
}
