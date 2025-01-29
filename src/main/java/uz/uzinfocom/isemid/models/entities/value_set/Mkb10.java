package uz.uzinfocom.isemid.models.entities.value_set;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Mkb10 extends BaseEntity {
    private String nullObj;
}
