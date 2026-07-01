package uz.uzinfocom.app.platform.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private UUID uuid;

    private Boolean active;

    @Column(length = 32)
    private String nnuzb;

    @Column(length = 200)
    private String username;

    @Column(name = "first_name", length = 200)
    private String firstName;

    @Column(name = "last_name", length = 200)
    private String lastName;

    @Column(name = "middle_name", length = 200)
    private String middleName;

    private LocalDate birthDate;

    @Column(name = "gender_code", length = 20)
    private String genderCode;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "country_code", length = 20)
    private String countryCode;

    @Column(name = "region_code", length = 20)
    private String regionCode;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    private String line;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> organizations = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    @Transient
    public Organization getCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseGet(() -> organizations.stream().findFirst().orElse(null));
    }

    public String getFullName() {
        return String.join(" ",
                nullToBlank(lastName),
                nullToBlank(firstName),
                nullToBlank(middleName)
        ).trim();
    }

    public boolean hasRole(String roleName) {
        if (roleName == null || roles == null) {
            return false;
        }

        String normalized = normalizeRole(roleName);
        return roles.stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .map(User::normalizeRole)
                .anyMatch(normalized::equals);
    }

    private static String normalizeRole(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("role_") ? normalized.substring("role_".length()) : normalized;
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
