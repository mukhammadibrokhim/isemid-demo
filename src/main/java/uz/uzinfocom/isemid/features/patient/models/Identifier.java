package uz.uzinfocom.isemid.features.patient.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        indexes = @Index(name = "idx_value_type", columnList = "value, typeCode"),
        name = "pt_identifier")
@EntityListeners(AuditingEntityListener.class)
public class Identifier extends BaseEntity {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 50)
    private String value;

    private String typeCode;

    private LocalDate periodStart;
    private LocalDate periodEnd;
}