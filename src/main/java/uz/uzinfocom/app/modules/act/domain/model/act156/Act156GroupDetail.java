package uz.uzinfocom.app.modules.act.domain.model.act156;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act156_group_detail",
        indexes = @Index(name = "idx_act156_group_detail_act156_id", columnList = "act156_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act156GroupDetail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act156_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act156_group_detail_act156"))
    private Act156 act156;

    @Column(name = "group_number", length = 50)
    private String groupNumber;

    @Column(name = "full_name_of_educator")
    private String fullNameOfEducator;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type_of_educator", length = 20)
    private CitizenLookupType identifierTypeOfEducator;

    @Column(name = "identifier_value_of_educator")
    private String identifierValueOfEducator;

    @Column(name = "hands_of_educator")
    private Boolean handsOfEducator;

    @Column(name = "first_food_bowl")
    private Boolean firstFoodBowl;

    @Column(name = "second_food_bowl")
    private Boolean secondFoodBowl;

    @Column(name = "tables")
    private Boolean tables;

    @Column(name = "chairs")
    private Boolean chairs;

    @Column(name = "window_sill")
    private Boolean windowSill;

    @Column(name = "door_handles")
    private Boolean doorHandles;

    @Column(name = "toys")
    private Boolean toys;

    @Column(name = "toy_shelf")
    private Boolean toyShelf;

    @Column(name = "carpets")
    private Boolean carpets;

    @Column(name = "clothes_rack")
    private Boolean clothesRack;

    @Column(name = "full_name_of_place_owner")
    private String fullNameOfPlaceOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type_of_place_owner", length = 20)
    private CitizenLookupType identifierTypeOfPlaceOwner;

    @Column(name = "identifier_value_of_place_owner")
    private String identifierValueOfPlaceOwner;

    @Column(name = "bed_clothes")
    private Boolean bedClothes;

    @Column(name = "bathroom_wall")
    private Boolean bathroomWall;

    @Column(name = "towels")
    private Boolean towels;

    @Column(name = "towel_rack")
    private Boolean towelRack;

    @Column(name = "water_tap_faucet")
    private Boolean waterTapFaucet;

    @Column(name = "wc_seats")
    private Boolean wcSeats;
}
