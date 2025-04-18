package uz.uzinfocom.isemid.features.card.dtos.requests.card205;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD205 type card")
public class Card205Request extends CardRequest {
    private String mkb10Name;
    private String mkb10Code;
    private Date epidemiologicalObservationDate;
    private Date veterinaryEmergencyInformationDate;
    private String addressOfBiteOccurrence;
    private Date dateOfBiteOccurrence;
    private String nameOfTreatmentPreventiveInstitution;
    private Date dateOfTreatmentPreventiveInstitution;
    private String animalType;
    private String whereAnimalComesFrom;
    private String whenAnimalAppeared;
    private Integer ageOfAnimal;
    private String breedOfAnimal;
    private String colourOfAnimal;
    private String individualSignsOfAnimal;
    private Integer certificateNumberOfFirstVetResults;
    private Date issueDateOfFirstCertificate;
    private Integer certificateNumberOfSecondVetResults;
    private Date issueDateOfSecondaryCertificate;
    private String dateTimeOfFeatherTaken;
    private String petRegisteredVetDepartment;
    private String additionalInformation;
    private String fullNameofAnimalOwner;
    //Catalog
    private String diagnosisInjuryCode;
    private List<InformationOtherBittenPeopleRequest> infoBittenPeople;
    private List<InformationOtherBittenAnimalRequest> infoOtherBittenAnimal;
    private List<InformationAboutAnimalBittenPeopleRequest> infoAbtAnimalBittenPeople;
    private String conditionOfAnimalCode;
    private String animalConservationCode;
    private String positionOfBittenVictimCode;
    private String dogOwnerComplianceCode;
}
