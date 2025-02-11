package uz.uzinfocom.isemid.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.enums.IdentifierType;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDate;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "idx_value_type", columnList = "value, typeCode"))
public class Identifier extends BaseEntity {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column( nullable = false, length = 50)
    private String value;

    @ManyToOne
    @JoinColumn(name = "type_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog typeCode;

    private LocalDate periodStart;
    private LocalDate periodEnd;
}