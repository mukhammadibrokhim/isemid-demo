package uz.uzinfocom.isemid.features.user.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    private String nnuzb;
    private String ppn;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String password;

    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate birthDate;
    private boolean active;
    private String genderCode;
    private String phoneNumber;
    private String countryCode;
    private String stateCode;
    private String cityCode;
    private String district;
    private String line;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Organization> organizations;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    private String positionCode;

    @Transient
    @JsonIgnore
    private Organization currentOrganization;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return nnuzb;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getFullName() {
        return String.join(" ", firstName, lastName, middleName);
    }

    public boolean hasRole(String roleName) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }
}
