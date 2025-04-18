package uz.uzinfocom.isemid.features.act.dtos.requests.act156;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act156GroupDetailRequest {
    private Long id;
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
