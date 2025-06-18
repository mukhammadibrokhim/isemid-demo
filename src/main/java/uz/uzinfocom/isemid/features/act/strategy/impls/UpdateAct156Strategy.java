package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156GroupDetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156KitchenUtensilRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act156.Act156GroupDetail;
import uz.uzinfocom.isemid.features.act.models.act156.Act156KitchenUtensil;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct156Strategy implements UpdateActStrategy {

    private final ActRepository actRepository;
    private final OrganizationService organizationService;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        Act act = validateAct(id, reqAct, actRepository);

        Act156 existingAct = (Act156) act;
        Act156Request reqAct156 = (Act156Request) reqAct;

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

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, null);
    }

    private Act156GroupDetail createNewAct156GroupDetail(Act156GroupDetailRequest reqDetail, Act156 existingAct) {
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

    private Act156GroupDetail updateAct156groupDetail(Act156GroupDetail existingDetail, Act156GroupDetailRequest reqDetail) {
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

    private Act156KitchenUtensil createNewAct156KitchenUtensil(Act156KitchenUtensilRequest reqDetail, Act156 existingAct) {
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

    private Act156KitchenUtensil updateAct156KitchenUtensil(Act156KitchenUtensil existingDetail, Act156KitchenUtensilRequest reqDetail) {
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