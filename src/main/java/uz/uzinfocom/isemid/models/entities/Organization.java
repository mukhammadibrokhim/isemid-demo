package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.util.ArrayList;
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
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;
    @Column(nullable = false,length = 500)
    private String name;
    private boolean active;
    private String tin;
    private String email;
    private String phone;
    private String type;

    @ManyToOne
    @JoinColumn(name = "level", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog level;

    @ManyToOne
    @JoinColumn(name = "country", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog country;

    @ManyToOne
    @JoinColumn(name = "state", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog state;

    @ManyToOne
    @JoinColumn(name = "city", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog city;

    private String district;
    private String line;

    @ManyToOne
    @JoinColumn(name = "service_area", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog serviceArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    @JsonIgnore
    private Organization parent;
    private Long parentId;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Organization> children = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties({"roles"})
    @JsonBackReference
    private List<User> users;
}
