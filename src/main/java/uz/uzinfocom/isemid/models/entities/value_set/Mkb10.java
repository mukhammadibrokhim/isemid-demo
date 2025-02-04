package uz.uzinfocom.isemid.models.entities.value_set;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_secondary_id", columnList = "secondaryId"),
                @Index(name = "idx_parent_id", columnList = "parent_id"),
                @Index(name = "idx_code", columnList = "code"),
                @Index(name = "idx_level", columnList = "level"),
                @Index(name = "idx_is_last_level", columnList = "isLastLevel"),
                @Index(name = "idx_name_uz", columnList = "nameUz"),
                @Index(name = "idx_name_en", columnList = "nameEn"),
                @Index(name = "idx_name_ru", columnList = "nameRu"),
                @Index(name = "idx_name_uz_cyrl", columnList = "nameUzCyrl")
        }
)
@Entity
public class Mkb10 {

    @Id
    private Long id;
    private Long secondaryId;
    //    @ManyToOne
//    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
//    @JsonBackReference
//    private Mkb10 parentId;
    private Long parentId;
    private String code;
    private int level;
    private boolean isLastLevel;
    private String nameUz;
    private String nameRu;
    private String nameUzCyril;
    private String nameKaa;
    private String comment;


    @OneToMany(mappedBy = "parentId")
    private List<Mkb10> children = new ArrayList<>();
}
