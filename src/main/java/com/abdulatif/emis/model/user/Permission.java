package com.abdulatif.emis.model.user;


import javax.persistence.*;

import com.abdulatif.emis.model.util.Auditable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sys_permissions")
public class Permission extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "PERMISSION_SEQUENCE")
    @SequenceGenerator(sequenceName = "PERMISSION_SEQUENCE", allocationSize = 1, name = "PERMISSION_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "type")
    private String type;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

    @Override
    public String toString() {
        return "Permission{" +
                "id= " + id + ", " +
                "name= " + name + ", " +
                "nameUz= " + nameUz + ", " +
                "nameRu= " + nameRu + ", " +
                "nameEn= " + nameEn + ", " +
                "type= " + type + "}";
    }
}

