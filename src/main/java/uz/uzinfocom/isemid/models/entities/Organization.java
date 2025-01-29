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
    @Column(length = 100)
    private String level;
    private String type;

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
    private String serviceArea;

    private String hierarchy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Organization parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
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
