package com.abdulatif.emis.model.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@AllArgsConstructor
@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "DISTRICT_SEQUENCE")
    @SequenceGenerator(sequenceName = "DISTRICT_SEQUENCE", allocationSize = 1, name = "DISTRICT_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @JsonIgnore
    private Region region;

}
