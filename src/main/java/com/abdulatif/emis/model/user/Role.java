package com.abdulatif.emis.model.user;


import com.abdulatif.emis.model.util.Auditable;
import com.abdulatif.emis.model.enums.Status;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "sys_roles")
public class Role extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "ROLE_SEQUENCE")
    @SequenceGenerator(sequenceName = "ROLE_SEQUENCE", allocationSize = 1, name = "ROLE_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Enumerated(STRING)
    @Column(name = "status")
    private Status status;

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "role_permissions",
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")})
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @Override
    public String toString() {
        return "Role{" +
                "id: " + id + ", " +
                "name: " + name + ", " +
                "code: " + code + "}";
    }
}
