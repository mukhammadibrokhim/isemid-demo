package uz.uzinfocom.isemid.models.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Permission extends BaseEntity {

    private String name;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionUzCyril;
    private String descriptionKaaUz; //Karakalpak lang
}
