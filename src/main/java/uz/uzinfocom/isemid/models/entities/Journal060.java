package uz.uzinfocom.isemid.models.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Journal060 extends AbsEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form058_id", nullable = false)
    private Form058 form058;
}
