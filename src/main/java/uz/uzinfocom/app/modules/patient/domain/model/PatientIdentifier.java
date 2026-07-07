package uz.uzinfocom.app.modules.patient.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pt_identifier",
        indexes = {
                @Index(
                        name = "idx_pt_identifier_patient_id",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_pt_identifier_type_value",
                        columnList = "type_code,value"
                ),
                @Index(
                        name = "idx_pt_identifier_value",
                        columnList = "value,patient_id"
                )
        }
)
public class PatientIdentifier extends AbsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "type_code", nullable = false, length = 30)
    private String typeCode;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;
}