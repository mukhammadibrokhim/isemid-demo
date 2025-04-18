package uz.uzinfocom.isemid.features.user.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.util.Set;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private boolean isActive = true;
    private String descriptionUz;
    private String descriptionUzCyril;
    private String descriptionRu;
    private String descriptionKaa;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @JsonIgnoreProperties({"createdAt", "updatedAt", "createdBy", "updatedBy", "descriptionUz", "descriptionRu", "descriptionUzCyril", "descriptionKaa"})
    private Set<Permission> permissions;
}
