package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act153.Act153;
import uz.uzinfocom.isemid.models.entities.act.act153.Act153Detail;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.mappers.act.ActMapper;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act153.ReqAct153;
import uz.uzinfocom.isemid.models.payload.requests.act.act153.ReqAct153Detail;
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
import static uz.uzinfocom.isemid.models.entities.enums.CatalogType.act153DetailObj;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct153Strategy implements UpdateActStrategy {

    final CatalogService catalogService;
    final ActRepository actRepository;
    final OrganizationService organizationService;
    final ActMapper actMapper;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }

    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Act153 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }
        Act153 existingAct = (Act153) act;
        ReqAct153 reqAct153 = (ReqAct153) reqAct;

        Optional.ofNullable(reqAct153.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct153.getSampleObjectName()).ifPresent(existingAct::setSampleObjectName);
        Optional.ofNullable(reqAct153.getSamplingDocuments()).ifPresent(existingAct::setSamplingDocuments);
        Optional.ofNullable(reqAct153.getSamplerFullName()).ifPresent(existingAct::setSamplerFullName);
        Optional.ofNullable(reqAct153.getPosition()).ifPresent(existingAct::setPosition);
        Optional.ofNullable(reqAct153.getParticipantFullName()).ifPresent(existingAct::setParticipantFullName);
        Optional.ofNullable(reqAct153.getParticipantPosition()).ifPresent(existingAct::setParticipantPosition);
        Optional.ofNullable(reqAct153.getAdditionalInfo()).ifPresent(existingAct::setAdditionalInfo);
        Optional.ofNullable(reqAct153.getAct153Details()).ifPresent(reqAct153Details -> {
            Set<Act153Detail> existingDetails = existingAct.getAct153Details();

            Set<Act153Detail> updatedDetails = reqAct153Details.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateExistingAct153Detail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct153Detail(reqDetail, existingAct));
                        } else {
                            return createNewAct153Detail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.getAct153Details().addAll(updatedDetails);
        });

        return new ApiResponse<>(true, "Success", actMapper.toDto(actRepository.save(act)));
    }

    private Act153Detail updateExistingAct153Detail(Act153Detail existingDetail, ReqAct153Detail reqDetail) {
        Optional.ofNullable(reqDetail.getObjectCode()).ifPresent(objCode -> {
            existingDetail.setObjectCode(objCode);
            getCatalogByCode(act153DetailObj, objCode);
        });
        Optional.ofNullable(reqDetail.getObjectCode()).ifPresent(existingDetail::setObjectCode);
        Optional.ofNullable(reqDetail.getSamplingPurpose()).ifPresent(existingDetail::setSamplingPurpose);
        Optional.ofNullable(reqDetail.getSamplingPurposeLoinc()).ifPresent(existingDetail::setSamplingPurposeLoinc);
        Optional.ofNullable(reqDetail.getAddress()).ifPresent(existingDetail::setAddress);
        Optional.ofNullable(reqDetail.getSamplingDepth()).ifPresent(existingDetail::setSamplingDepth);
        Optional.ofNullable(reqDetail.getDistanceFromShore()).ifPresent(existingDetail::setDistanceFromShore);
        Optional.ofNullable(reqDetail.getSampleVolume()).ifPresent(existingDetail::setSampleVolume);
        Optional.ofNullable(reqDetail.getSampleLocation()).ifPresent(existingDetail::setSampleLocation);
        Optional.ofNullable(reqDetail.getWeatherAtSampling()).ifPresent(existingDetail::setWeatherAtSampling);
        Optional.ofNullable(reqDetail.getWaterTemperature()).ifPresent(existingDetail::setWaterTemperature);
        Optional.ofNullable(reqDetail.getSampleType()).ifPresent(existingDetail::setSampleType);
        Optional.ofNullable(reqDetail.getSamplingDate()).ifPresent(existingDetail::setSamplingDate);
        Optional.ofNullable(reqDetail.getSpecialSamplingConditions()).ifPresent(existingDetail::setSpecialSamplingConditions);
        Optional.ofNullable(reqDetail.getOrganizationId()).ifPresent(orgId -> {
            existingDetail.setOrganizationId(orgId);
            organizationService.findById(orgId).ifPresent(existingDetail::setOrganization);
        });
        Optional.ofNullable(reqDetail.getLaboratoryAddress()).ifPresent(existingDetail::setLaboratoryAddress);
        Optional.ofNullable(reqDetail.getDeliveryConditions()).ifPresent(existingDetail::setDeliveryConditions);
        Optional.ofNullable(reqDetail.getStorageConditions()).ifPresent(existingDetail::setStorageConditions);
        Optional.ofNullable(reqDetail.getConservationMethods()).ifPresent(existingDetail::setConservationMethods);
        Optional.ofNullable(reqDetail.getAdditionalInfo()).ifPresent(existingDetail::setAdditionalInfo);
        return existingDetail;
    }

    private Act153Detail createNewAct153Detail(ReqAct153Detail reqDetail, Act153 existingAct) {
        return Act153Detail.builder()
                .act153(existingAct)
                .object(getCatalogByCode(act153DetailObj, reqDetail.getObjectCode()))
                .objectCode(reqDetail.getObjectCode())
                .samplingPurpose(reqDetail.getSamplingPurpose())
                .samplingPurposeLoinc(reqDetail.getSamplingPurposeLoinc())
                .address(reqDetail.getAddress())
                .samplingDepth(reqDetail.getSamplingDepth())
                .distanceFromShore(reqDetail.getDistanceFromShore())
                .sampleVolume(reqDetail.getSampleVolume())
                .sampleLocation(reqDetail.getSampleLocation())
                .weatherAtSampling(reqDetail.getWeatherAtSampling())
                .waterTemperature(reqDetail.getWaterTemperature())
                .sampleType(reqDetail.getSampleType())
                .samplingDate(reqDetail.getSamplingDate())
                .specialSamplingConditions(reqDetail.getSpecialSamplingConditions())
                .organization(organizationService.findById(reqDetail.getOrganizationId()).orElse(null))
                .organizationId(reqDetail.getOrganizationId())
                .laboratoryAddress(reqDetail.getLaboratoryAddress())
                .deliveryConditions(reqDetail.getDeliveryConditions())
                .storageConditions(reqDetail.getStorageConditions())
                .conservationMethods(reqDetail.getConservationMethods())
                .additionalInfo(reqDetail.getAdditionalInfo())
                .build();
    }
}
