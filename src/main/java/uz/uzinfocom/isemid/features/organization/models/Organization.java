package uz.uzinfocom.isemid.features.organization.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.organization.models.enums.OrganizationType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, length = 500)
    private String name;

    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private String type;
    private String levelCode;
    private String countryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_code", referencedColumnName = "code")
    private Catalog state;

    @Column(name = "state_code", updatable = false, insertable = false)
    private String stateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_code", referencedColumnName = "code")
    private Catalog city;

    @Column(name = "city_code", updatable = false, insertable = false)
    private String cityCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrganizationType organizationType = OrganizationType.NONE;

    private String district;
    private String line;
    private String serviceAreaCode;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Organization> children;

    @ManyToOne
    @JsonBackReference
    private Organization parent;

    @Column(name = "parent_id",insertable = false, updatable = false)
    private Long parentId;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonBackReference
    private List<User> users;
}
