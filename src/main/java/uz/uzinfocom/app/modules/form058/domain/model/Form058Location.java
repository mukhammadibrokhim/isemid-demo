package uz.uzinfocom.app.modules.form058.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "fm058_location")
@Table(name = "fm058_location")
public class Form058Location extends AuditableEntity {

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location", length = 1000)
    private String location;
}
