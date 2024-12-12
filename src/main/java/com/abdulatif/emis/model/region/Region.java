package com.abdulatif.emis.model.region;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "REGION_SEQUENCE")
    @SequenceGenerator(sequenceName = "REGION_SEQUENCE", allocationSize = 1, name = "REGION_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

}
