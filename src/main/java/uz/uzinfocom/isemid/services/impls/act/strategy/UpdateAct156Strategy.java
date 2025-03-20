package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act156.Act156;
import uz.uzinfocom.isemid.models.entities.act.act156.Act156GroupDetail;
import uz.uzinfocom.isemid.models.entities.act.act156.Act156KitchenUtensil;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act156.ReqAct156;
import uz.uzinfocom.isemid.models.payload.requests.act.act156.ReqAct156GroupDetail;
import uz.uzinfocom.isemid.models.payload.requests.act.act156.ReqAct156KitchenUtensil;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct156Strategy implements UpdateActStrategy {

    private final CatalogService catalogService;
    private final ActRepository actRepository;
    private final OrganizationService organizationService;

    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Act156 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }

        Act156 existingAct = (Act156) act;
        ReqAct156 reqAct156 = (ReqAct156) reqAct;

        Optional.ofNullable(reqAct156.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct156.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct156.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct156.getInstitutionName()).ifPresent(existingAct::setInstitutionName);
        Optional.ofNullable(reqAct156.getInstitutionAddress()).ifPresent(existingAct::setInstitutionAddress);
        Optional.ofNullable(reqAct156.getSampleTakenTime()).ifPresent(existingAct::setSampleTakenTime);
        Optional.ofNullable(reqAct156.getOrganizationId()).ifPresent(orgId -> {
            existingAct.setOrganizationId(orgId);
            organizationService.findById(orgId).ifPresent(existingAct::setOrganization);
        });
        Optional.ofNullable(reqAct156.getLaboratoryAddress()).ifPresent(existingAct::setLaboratoryAddress);
        Optional.ofNullable(reqAct156.getSampleDeliveryTime()).ifPresent(existingAct::setSampleDeliveryTime);
        Optional.ofNullable(reqAct156.getFullNameofSampler()).ifPresent(existingAct::setFullNameofSampler);
        Optional.ofNullable(reqAct156.getPositionOfSampler()).ifPresent(existingAct::setPositionOfSampler);
        Optional.ofNullable(reqAct156.getFullNameOfObjectRepresentative()).ifPresent(existingAct::setFullNameOfObjectRepresentative);
        Optional.ofNullable(reqAct156.getPositionOfObjectRepresentative()).ifPresent(existingAct::setPositionOfObjectRepresentative);
        Optional.ofNullable(reqAct156.getAct156GroupDetails()).ifPresent(reqAct156GroupDetails -> {
            Set<Act156GroupDetail> existingDetails = existingAct.getAct156GroupDetails();

            Set<Act156GroupDetail> updatedDetails = reqAct156GroupDetails.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateAct156groupDetail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct156GroupDetail(reqDetail, existingAct));
                        } else {
                            return createNewAct156GroupDetail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.setAct156GroupDetails(updatedDetails);
        });

        Optional.ofNullable(reqAct156.getAct156KitchenUtensils()).ifPresent(reqAct156KitchenUtensils -> {
            Set<Act156KitchenUtensil> existingDetails = existingAct.getAct156KitchenUtensils();

            Set<Act156KitchenUtensil> updatedDetails = reqAct156KitchenUtensils.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateAct156KitchenUtensil(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct156KitchenUtensil(reqDetail, existingAct));
                        } else {
                            return createNewAct156KitchenUtensil(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.setAct156KitchenUtensils(updatedDetails);
        });

        return new ApiResponse<>(true, "Success", null);
    }

    private Act156GroupDetail createNewAct156GroupDetail(ReqAct156GroupDetail reqDetail, Act156 existingAct) {
        return Act156GroupDetail.builder()
                .fullNameOfEducator(reqDetail.getFullNameOfEducator())
                .handsOfEducator(reqDetail.getHandsOfEducator())
                .firstFoodBowl(reqDetail.getFirstFoodBowl())
                .secondFoodBowl(reqDetail.getSecondFoodBowl())
                .tables(reqDetail.getTables())
                .chairs(reqDetail.getChairs())
                .windowSill(reqDetail.getWindowSill())
                .doorHandles(reqDetail.getDoorHandles())
                .toys(reqDetail.getToys())
                .toyShelf(reqDetail.getToyShelf())
                .carpets(reqDetail.getCarpets())
                .clothesRack(reqDetail.getClothesRack())
                .fullNameOfPlaceOwner(reqDetail.getFullNameOfPlaceOwner())
                .bedClothes(reqDetail.getBedClothes())
                .bathroomWall(reqDetail.getBathroomWall())
                .towels(reqDetail.getTowels())
                .towelRack(reqDetail.getTowelRack())
                .waterTapFaucet(reqDetail.getWaterTapFaucet())
                .WCSeats(reqDetail.getWCSeats())
                .build();
    }

    private Act156GroupDetail updateAct156groupDetail(Act156GroupDetail existingDetail, ReqAct156GroupDetail reqDetail) {
        Optional.ofNullable(reqDetail.getFullNameOfEducator()).ifPresent(existingDetail::setFullNameOfEducator);
        Optional.ofNullable(reqDetail.getHandsOfEducator()).ifPresent(existingDetail::setHandsOfEducator);
        Optional.ofNullable(reqDetail.getFirstFoodBowl()).ifPresent(existingDetail::setFirstFoodBowl);
        Optional.ofNullable(reqDetail.getSecondFoodBowl()).ifPresent(existingDetail::setSecondFoodBowl);
        Optional.ofNullable(reqDetail.getTables()).ifPresent(existingDetail::setTables);
        Optional.ofNullable(reqDetail.getChairs()).ifPresent(existingDetail::setChairs);
        Optional.ofNullable(reqDetail.getWindowSill()).ifPresent(existingDetail::setWindowSill);
        Optional.ofNullable(reqDetail.getDoorHandles()).ifPresent(existingDetail::setDoorHandles);
        Optional.ofNullable(reqDetail.getToys()).ifPresent(existingDetail::setToys);
        Optional.ofNullable(reqDetail.getToyShelf()).ifPresent(existingDetail::setToyShelf);
        Optional.ofNullable(reqDetail.getCarpets()).ifPresent(existingDetail::setCarpets);
        Optional.ofNullable(reqDetail.getClothesRack()).ifPresent(existingDetail::setClothesRack);
        Optional.ofNullable(reqDetail.getFullNameOfPlaceOwner()).ifPresent(existingDetail::setFullNameOfPlaceOwner);
        Optional.ofNullable(reqDetail.getBedClothes()).ifPresent(existingDetail::setBedClothes);
        Optional.ofNullable(reqDetail.getBathroomWall()).ifPresent(existingDetail::setBathroomWall);
        Optional.ofNullable(reqDetail.getTowels()).ifPresent(existingDetail::setTowels);
        Optional.ofNullable(reqDetail.getTowelRack()).ifPresent(existingDetail::setTowelRack);
        Optional.ofNullable(reqDetail.getWaterTapFaucet()).ifPresent(existingDetail::setWaterTapFaucet);
        Optional.ofNullable(reqDetail.getWCSeats()).ifPresent(existingDetail::setWCSeats);
        return existingDetail;
    }

    private Act156KitchenUtensil createNewAct156KitchenUtensil(ReqAct156KitchenUtensil reqDetail, Act156 existingAct) {
        return Act156KitchenUtensil.builder()
                .knifeForBread(reqDetail.getKnifeForBread())
                .fruitCuttingBoard(reqDetail.getFruitCuttingBoard())
                .distributionTable(reqDetail.getDistributionTable())
                .containerForFinishedProducts(reqDetail.getContainerForFinishedProducts())
                .fullNameOfChef(reqDetail.getFullNameOfChef())
                .handsOfChef(reqDetail.getHandsOfChef())
                .clothesOfChef(reqDetail.getClothesOfChef())
                .build();
    }

    private Act156KitchenUtensil updateAct156KitchenUtensil(Act156KitchenUtensil existingDetail, ReqAct156KitchenUtensil reqDetail) {
        Optional.ofNullable(reqDetail.getKnifeForBread()).ifPresent(existingDetail::setKnifeForBread);
        Optional.ofNullable(reqDetail.getFruitCuttingBoard()).ifPresent(existingDetail::setFruitCuttingBoard);
        Optional.ofNullable(reqDetail.getDistributionTable()).ifPresent(existingDetail::setDistributionTable);
        Optional.ofNullable(reqDetail.getContainerForFinishedProducts()).ifPresent(existingDetail::setContainerForFinishedProducts);
        Optional.ofNullable(reqDetail.getFullNameOfChef()).ifPresent(existingDetail::setFullNameOfChef);
        Optional.ofNullable(reqDetail.getHandsOfChef()).ifPresent(existingDetail::setHandsOfChef);
        Optional.ofNullable(reqDetail.getClothesOfChef()).ifPresent(existingDetail::setClothesOfChef);
        return existingDetail;
    }
}