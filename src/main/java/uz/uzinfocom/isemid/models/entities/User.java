package uz.uzinfocom.isemid.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    private String nnuzb;
    private String ppn;

    private String firstName;
    private String lastName;
    private String middleName;

    private LocalDate birthDate;
    private boolean active;
    private String gender;

    private String phoneNumber;

    private String country;
    private String state;
    private String city;
    private String district;
    private String line;
    @Column(length = 100)
    private String accessAreaCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonManagedReference
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    private List<Organization> organizations;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "catalog_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog position;

    @Transient
    @JsonIgnore
    private Organization currentOrganization;
}
