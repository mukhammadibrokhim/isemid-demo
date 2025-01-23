package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
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
    private String tin;
    private String email;
    private String phone;
    private String type;
    private String country;
    private String state;
    private String city;
    private String district;
    private String line;
    private String serviceArea;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_organizations",
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties({"roles"})
    @JsonManagedReference
    private List<User> users;
}
