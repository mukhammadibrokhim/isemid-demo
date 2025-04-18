package uz.uzinfocom.isemid.features.classifier.models;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_id", columnList = "id"),
        @Index(name = "idx_short_name", columnList = "shortName"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_include_total", columnList = "includeInTotal"),
        @Index(name = "idx_name_ru", columnList = "nameRu"),
        @Index(name = "idx_name_uz", columnList = "nameUz"),
        @Index(name = "idx_name_uz_cyril", columnList = "nameUzCyril"),
        @Index(name = "idx_name_kaa", columnList = "nameKaa"),
},name = "cls_manual_report")
public class ManualReport extends BaseEntity {
    private String code;
    private String shortName;
    private String nameUz;
    private String nameUzCyril;
    private String nameRu;
    private String nameKaa;
    private Boolean includeInTotal;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cls_manual_report_id")
    private List<Mkb10> mkb10Codes;
}
