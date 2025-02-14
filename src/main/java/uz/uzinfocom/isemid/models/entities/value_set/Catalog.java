package uz.uzinfocom.isemid.models.entities.value_set;

import jakarta.persistence.*;
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
@Table(indexes = {
        @Index(name = "idx_id", columnList = "id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_parent_code", columnList = "parentCode"),
        @Index(name = "idx_name_ru", columnList = "nameRu"),
        @Index(name = "idx_name_uz", columnList = "nameUz"),
        @Index(name = "idx_name_uz_cyril", columnList = "nameUzCyril"),
        @Index(name = "idx_name_kaa", columnList = "nameKaa"),
})
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
