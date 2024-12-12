package com.abdulatif.emis.model.user;


import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.region.Region;
import com.abdulatif.emis.model.util.Auditable;
import com.abdulatif.emis.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "sys_users")
@Builder
public class User extends Auditable<String> implements UserDetails {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "USER_SEQUENCE")
    @SequenceGenerator(sequenceName = "USER_SEQUENCE", allocationSize = 1, name = "USER_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_serial")
    private String passportSerial;

    @Column(name = "pnfl")
    private String pnfl;

    @Column(name = "inn")
    private String inn;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "date_of_birth")
    @Temporal(value = TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    protected Date dateOfBirth;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    private Boolean gender;

    private String nationality;

    @OneToOne
    private District district;

    @OneToOne
    private Region region;

    private Boolean isPatient = false;

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "user_organizations",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "organization_id", referencedColumnName = "id")})
    private Set<Organization> parentOrganizations = new HashSet<>();

    public void setUsername(String username) {
        this.username = username.trim();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}



