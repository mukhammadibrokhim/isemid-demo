package uz.uzinfocom.isemid.features.card.models.card174;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card174_disinfection_info")
public class DisinfectionInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "card174_id", nullable = false)
    private Card174 card174;

    @ElementCollection
    @CollectionTable(name = "card174_disinfection_factors", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> disinfectionFactors = new ArrayList<>(); // Dezinfeksiya ob'ektlari

    private Integer disinfectedCount; // Zararsizlantirilgan omillar miqdori
    private LocalDate disinfectionDate; // Zararsizlantirish sanasi

    @ElementCollection
    @CollectionTable(name = "card174_elimination_method", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> eliminationMethods = new ArrayList<>(); // Yo‘q qilish usullari

    private String eventLocation; // Hodisa joyi
    private String controlResults; // Chora nazorati natijasi
}
