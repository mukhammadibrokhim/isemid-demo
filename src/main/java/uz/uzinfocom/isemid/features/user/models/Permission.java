package uz.uzinfocom.isemid.features.user.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

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

    public Permission(String subject, List<String> actions) {
        this.subject = subject;
        this.actions = actions;
    }
}
