package uz.uzinfocom.isemid.models.entities.value_set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Catalog extends BaseEntity {
    @Column(nullable = false)
    private String type; //catalogType
    @Column(nullable = false, unique = true)
    private String code;
    private String parentCode;
    @Column(length = 1000)
    private String nameRu;
    @Column(length = 1000)
    private String nameUz;
    @Column(length = 1000)
    private String nameUzCyril;
    @Column(length = 1000)
    private String nameKaa; //Karakalpak tili


}
