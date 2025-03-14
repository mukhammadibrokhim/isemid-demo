package uz.uzinfocom.isemid.models.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Permission extends BaseEntity {
    private String subject;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permission_actions", joinColumns = @JoinColumn(name = "permission_id"))
    @Column(name = "action")
    private List<String> actions;

    private boolean active = true;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionUzCyril;
    private String descriptionKaa;
}
