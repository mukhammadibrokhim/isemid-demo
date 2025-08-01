package uz.uzinfocom.isemid.features.organization.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.models.enums.ServiceType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_service_types", joinColumns = @JoinColumn(name = "organization_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private List<ServiceType> serviceTypes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "level_type")
    private LevelType levelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_type")
    private MedicalType medicalType;

    @Column(name = "country_code")
    private String countryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_code", referencedColumnName = "code", updatable = false, insertable = false)
    private Catalog state;

    @Column(name = "state_code")
    private String stateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_code", referencedColumnName = "code", updatable = false, insertable = false)
    private Catalog city;

    @Column(name = "city_code")
    private String cityCode;

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
