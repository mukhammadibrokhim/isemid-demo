package uz.uzinfocom.isemid.features.form058.models;


import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "fm058_location")
public class Location extends BaseEntity {
    private Double latitude;
    private Double longitude;
    private String location;
}
