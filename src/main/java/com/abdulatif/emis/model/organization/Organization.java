package com.abdulatif.emis.model.organization;


import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.model.util.Auditable;
import com.abdulatif.emis.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "organizations")
public class Organization extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "ORGANIZATION_SEQUENCE")
    @SequenceGenerator(sequenceName = "ORGANIZATION_SEQUENCE", allocationSize = 1, name = "ORGANIZATION_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "address")
    private String address;

    @Column(name = "hierarchy")
    private String hierarchy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Organization parent;

    @ManyToMany(mappedBy = "parentOrganizations", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @Enumerated(STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "uuid")
    private String uuid;
}
