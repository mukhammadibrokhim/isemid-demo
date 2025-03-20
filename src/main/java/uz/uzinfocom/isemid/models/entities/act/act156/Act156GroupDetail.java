package uz.uzinfocom.isemid.models.entities.act.act156;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act156_group_details")
public class Act156GroupDetail extends BaseEntity {
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