package uz.uzinfocom.isemid.models.entities.value_set;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.util.List;


@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Mkb10 extends BaseEntity {

    private String mkb10Code;
    private String nameUz;
    private String nameUzCyril;
    private String nameRU;
    private String codeRange;
    private String parentCodeRange;


    @OneToMany(mappedBy = "parentMkb10")
    private List<Mkb10> children;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Mkb10 parentMkb10;
}
