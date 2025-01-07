package uz.uzinfocom.isemid.models.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;
    @Column(nullable = false,length = 500)
    private String name;
    private boolean active;
    private String email;
    private String phone;
    private String type;
    private String country;
    private String state;
    private String city;
    private String district;
    private String line;
    private String serviceArea;
    private String tin;
}
