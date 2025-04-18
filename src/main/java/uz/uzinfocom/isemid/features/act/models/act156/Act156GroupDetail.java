package uz.uzinfocom.isemid.features.act.models.act156;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act156_group_details")
public class Act156GroupDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act156_id")  // Foreign key to Act154
    private Act156 act156;

    private String fullNameOfEducator;
    private Boolean handsOfEducator;
    private Boolean firstFoodBowl;
    private Boolean secondFoodBowl;
    private Boolean tables;
    private Boolean chairs;
    private Boolean windowSill;
    private Boolean doorHandles;
    private Boolean toys;
    private Boolean toyShelf;
    private Boolean carpets;
    private Boolean clothesRack;
    private String fullNameOfPlaceOwner;
    private Boolean bedClothes;
    private Boolean bathroomWall;
    private Boolean towels;
    private Boolean towelRack;
    private Boolean waterTapFaucet;
    private Boolean WCSeats;
}