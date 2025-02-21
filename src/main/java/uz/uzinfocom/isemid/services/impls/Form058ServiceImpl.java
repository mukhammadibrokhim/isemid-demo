package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.enums.DocumentStatus;
import uz.uzinfocom.isemid.models.entities.enums.FilterType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058AssignCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.repositories.Journal060Repository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.Form058Service;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.UserService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    final Form058Repository form058Repository;
    final ModelMapper modelMapper;
    final Mkb10Service mkb10Service;
    final Mkb10Repository mkb10Repository;
    final UserService userService;
    final CatalogService catalogService;
    final OrganizationService organizationService;
    final Journal060Repository journal060Repository;
    final CardRepository cardRepository;

    private Catalog mapCatalog(CatalogType type, String code) {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }

    @Override
    public ApiResponse<Form058> createForm058(ReqForm058 reqForm058, User user) {
        Patient patient = Patient.builder()
                .firstName(reqForm058.getPatient().getFirstName())
                .lastName(reqForm058.getPatient().getLastName())
                .middleName(reqForm058.getPatient().getMiddleName())
                .birthDate(reqForm058.getPatient().getBirthDate())
                .gender(catalogService.findByTypeAndCode(CatalogType.gender, reqForm058.getPatient().getGender().name()).orElse(null))
                .workPlace(reqForm058.getPatient().getWorkPlace())
                .workPlaceState(mapCatalog(CatalogType.state, reqForm058.getPatient().getWorkPlaceStateCode()))
                .workPlaceCity(mapCatalog(CatalogType.city, reqForm058.getPatient().getWorkPlaceCityCode()))
                .residentialStatus(mapCatalog(CatalogType.residentialStatus, reqForm058.getPatient().getResidentialStatus().name()))
                .category(catalogService.findByTypeAndCode(CatalogType.category, reqForm058.getPatient().getCategoryCode()).orElse(null))
                .profession(catalogService.findByTypeAndCode(CatalogType.profession, reqForm058.getPatient().getProfessionCode()).orElse(null))
                .populationType(catalogService.findByTypeAndCode(CatalogType.populationType, reqForm058.getPatient().getPopulationType().name()).orElse(null))
                .maritalStatus(mapCatalog(CatalogType.maritalStatus, reqForm058.getPatient().getMaritalStatus().name()))
                .build();

        List<Identifier> identifiers = reqForm058.getPatient().getIdentifiers().stream().map(reqIdentifier -> {
            Identifier identifier = Identifier.builder()
                    .value(reqIdentifier.getValue())
                    .typeCode(catalogService.findByTypeAndCode(CatalogType.identifierType, reqIdentifier.getTypeCode().name()).orElse(null))
                    .periodEnd(reqIdentifier.getPeriodEnd())
                    .periodStart(reqIdentifier.getPeriodStart())
                    .build();
            identifier.setPatient(patient);
            return identifier;
        }).toList();
        patient.setIdentifiers(identifiers);

        List<Address> addresses = reqForm058.getPatient().getAddresses().stream().map(reqAddress -> {
            Address address = Address.builder()
                    .state(catalogService.findByTypeAndCode(CatalogType.state, reqAddress.getStateCode()).orElse(null))
                    .city(catalogService.findByTypeAndCode(CatalogType.city, reqAddress.getCityCode()).orElse(null))
                    .neighborhood(catalogService.findByTypeAndCode(CatalogType.neighborhood, reqAddress.getNeighborhood()).orElse(null))
                    .streetAddress(reqAddress.getStreetAddress())
                    .houseNumber(reqAddress.getHouseNumber())
                    .apartmentNumber(reqAddress.getApartmentNumber())
                    .type(reqAddress.getType())
                    .build();
            address.setPatient(patient);
            return address;
        }).toList();
        patient.setAddresses(addresses);
        User reporter = userService.findByUuid(user.getUuid()).orElse(null);

        Form058 form058 = Form058.builder()
                .mkb10Code(reqForm058.getMkb10Code())
                .mkb10Name(reqForm058.getMkb10Name())
                .mkb10UsageLimit(reqForm058.getMkb10UsageLimit())
                .patient(patient)
                .labConfirmation(reqForm058.getLabConfirmation())
                .hospitalPlace(organizationService.findById(reqForm058.getHospitalPlaceId()).orElse(null))
                .admissionDate(reqForm058.getAdmissionDate())
                .diseaseDate(reqForm058.getDiseaseDate())
                .firstVisitDate(reqForm058.getFirstVisitDate())
                .diagnosisDate(reqForm058.getDiagnosisDate())
                .visitDate(reqForm058.getVisitDate())
                .senderOrganization(organizationService.findById(reqForm058.getSenderOrganizationId()).orElse(null))
                .receiverOrganization(organizationService.findById(reqForm058.getReceiverOrganizationId()).orElse(null))
                .location(Location.builder()
                        .latitude(reqForm058.getLocation().getLatitude())
                        .longitude(reqForm058.getLocation().getLongitude())
                        .location(reqForm058.getLocation().getLocation())
                        .build())
                .diseasePlace(mapCatalog(CatalogType.diseasePlace, reqForm058.getDiseasePlaceCode()))
                .diseaseCause(reqForm058.getDiseaseCause())
                .epidemicMeasures(reqForm058.getEpidemicMeasures())
                .journalForm(catalogService.findByTypeAndCode(CatalogType.formType, reqForm058.getJournalFormCode()).orElse(null))
                .notifierFullName(reqForm058.getNotifierFullName())
                .comment(reqForm058.getComment())
                .initialReportDateTime(reqForm058.getInitialReportDateTime())
                .docSendDate(reqForm058.getDocSendDate())
                .reporter(reporter)
                .build();
        form058Repository.save(form058);
        Journal060 journal060 = new Journal060();
        journal060.setForm058(form058);
        journal060Repository.save(journal060);
        return new ApiResponse<>(true, "Форма 058 успешно создана.", form058);
    }

    @Override
    public ApiResponse<Form058> updateForm058(ReqForm058 reqForm058, Long id, User user) {
        Form058 existedForm058 = form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Form058 not found."));
        Optional.ofNullable(reqForm058.getMkb10Code()).ifPresent(existedForm058::setMkb10Code);
        Optional.ofNullable(reqForm058.getMkb10Name()).ifPresent(existedForm058::setMkb10Name);
        Optional.ofNullable(reqForm058.getMkb10UsageLimit()).ifPresent(existedForm058::setMkb10UsageLimit);
        Optional.ofNullable(reqForm058.getPatient()).ifPresent(reqPatient -> {
            Patient patient = existedForm058.getPatient();
            if (patient != null) {
                Optional.ofNullable(reqPatient.getFirstName()).ifPresent(patient::setFirstName);
                Optional.ofNullable(reqPatient.getLastName()).ifPresent(patient::setLastName);
                Optional.ofNullable(reqPatient.getMiddleName()).ifPresent(patient::setMiddleName);
                Optional.ofNullable(reqPatient.getIdentifiers()).ifPresent(reqIdentifiers -> {
                    List<Identifier> exsistingIdentifiers = patient.getIdentifiers();


                    exsistingIdentifiers.forEach(existingIdentifier ->
                            reqIdentifiers.stream()
                                    .filter(reqIdentifier -> reqIdentifier.getTypeCode().name()
                                            .equals(existingIdentifier.getTypeCode().getType()))
                                    .findFirst()
                                    .ifPresent(reqIdentifier -> {
                                        existingIdentifier.setValue(reqIdentifier.getValue());
                                        existingIdentifier.setPeriodStart(reqIdentifier.getPeriodStart());
                                        existingIdentifier.setPeriodEnd(reqIdentifier.getPeriodEnd());
                                    })
                    );

                    reqIdentifiers.stream()
                            .filter(reqIdentifier -> exsistingIdentifiers.stream()
                                    .noneMatch(existingIdentifier -> existingIdentifier.getTypeCode().getCode()
                                            .equals(reqIdentifier.getTypeCode().name())))
                            .forEach(reqIdentifier -> {
                                Catalog typeCatalog = catalogService.findByTypeAndCode(
                                                CatalogType.identifierType, reqIdentifier.getTypeCode().name())
                                        .orElseThrow(() -> new ApiException(NOT_FOUND,
                                                "Identifier type not found: " + reqIdentifier.getTypeCode()));

                                Identifier newIdentifier = Identifier.builder()
                                        .patient(patient)
                                        .typeCode(typeCatalog)
                                        .value(reqIdentifier.getValue())
                                        .periodStart(reqIdentifier.getPeriodStart())
                                        .periodEnd(reqIdentifier.getPeriodEnd())
                                        .build();

                                patient.getIdentifiers().add(newIdentifier);
                            });

                });
                Optional.ofNullable(reqPatient.getBirthDate()).ifPresent(patient::setBirthDate);
                Optional.ofNullable(reqPatient.getAgeYears()).ifPresent(patient::setAgeYears);
                Optional.ofNullable(reqPatient.getAgeMonths()).ifPresent(patient::setAgeMonths);
                Optional.ofNullable(reqPatient.getGender()).ifPresent(reqGen -> mapCatalog(CatalogType.gender, reqGen.name()));
                Optional.ofNullable(reqPatient.getPhoneNumber()).ifPresent(patient::setPhoneNumber);
                Optional.ofNullable(reqPatient.getKinshipDegree()).ifPresent(patient::setKinshipDegree);
                Optional.ofNullable(reqPatient.getKinshipFullName()).ifPresent(patient::setKinshipFullName);
                Optional.ofNullable(reqPatient.getResidentialStatus()).ifPresent(reqResidentialStatus -> mapCatalog(CatalogType.residentialStatus, reqResidentialStatus.name()));
                Optional.ofNullable(reqPatient.getMaritalStatus()).ifPresent(reqMaritalStatus -> mapCatalog(CatalogType.maritalStatus, reqMaritalStatus.name()));
                Optional.ofNullable(reqPatient.getPopulationType()).ifPresent(reqPopulationType -> mapCatalog(CatalogType.populationType, reqPopulationType.name()));
                Optional.ofNullable(reqPatient.getCategoryCode()).ifPresent(reqCategoryCode -> mapCatalog(CatalogType.category, reqCategoryCode));
                Optional.ofNullable(reqPatient.getProfessionCode()).ifPresent(code -> mapCatalog(CatalogType.profession, code));
                Optional.ofNullable(reqPatient.getWorkPlace()).ifPresent(patient::setWorkPlace);
                Optional.ofNullable(reqPatient.getWorkPlaceStateCode()).ifPresent(code -> mapCatalog(CatalogType.state, code));
                Optional.ofNullable(reqPatient.getWorkPlaceCityCode()).ifPresent(code -> mapCatalog(CatalogType.city, code));
                Optional.ofNullable(reqPatient.getAddresses()).ifPresent(reqAddresses -> {

                    List<Address> existingAddresses = patient.getAddresses();

                    existingAddresses.forEach(existingAddress ->
                            reqAddresses.stream()
                                    .filter(reqAddress -> reqAddress.getType()
                                            .equals(existingAddress.getType()))
                                    .findFirst()
                                    .ifPresent(reqAddress -> {
                                        existingAddress.setState(mapCatalog(CatalogType.state, reqAddress.getStateCode()));
                                        existingAddress.setCity(mapCatalog(CatalogType.city, reqAddress.getCityCode()));
                                        existingAddress.setNeighborhood(mapCatalog(CatalogType.neighborhood, reqAddress.getNeighborhood()));
                                        existingAddress.setStreetAddress(reqAddress.getStreetAddress());
                                        existingAddress.setHouseNumber(reqAddress.getHouseNumber());
                                        existingAddress.setApartmentNumber(reqAddress.getApartmentNumber());
                                    })
                    );

                    reqAddresses.stream()
                            .filter(reqAddress -> existingAddresses.stream()
                                    .noneMatch(existingAddress -> existingAddress.getType()
                                            .equals(reqAddress.getType())))
                            .forEach(reqAddress -> {
//                                Catalog typeCatalog = catalogService.findByTypeAndCode(
//                                                CatalogType.addressType, reqAddress.getType().name())
//                                        .orElseThrow(() -> new ApiException(NOT_FOUND,
//                                                "Address type not found: " + reqAddress.getType().name()));

                                Address newAddress = Address.builder()
                                        .patient(patient)
                                        .type(reqAddress.getType())
                                        .state(mapCatalog(CatalogType.state, reqAddress.getStateCode()))
                                        .city(mapCatalog(CatalogType.city, reqAddress.getCityCode()))
                                        .neighborhood(mapCatalog(CatalogType.neighborhood, reqAddress.getNeighborhood()))
                                        .streetAddress(reqAddress.getStreetAddress())
                                        .houseNumber(reqAddress.getHouseNumber())
                                        .apartmentNumber(reqAddress.getApartmentNumber())
                                        .build();

                                patient.getAddresses().add(newAddress);
                            });
                });
            }
        });

        Optional.ofNullable(reqForm058.getLabConfirmation()).ifPresent(existedForm058::setLabConfirmation);

        Optional.ofNullable(reqForm058.getHospitalPlaceId()).ifPresent(orgId -> {
            Organization organization = organizationService.findById(orgId).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found for Hospital place."));
            existedForm058.setHospitalPlace(organization);
        });

        Optional.ofNullable(reqForm058.getAdmissionDate()).ifPresent(existedForm058::setAdmissionDate);
        Optional.ofNullable(reqForm058.getDiagnosisDate()).ifPresent(existedForm058::setDiagnosisDate);
        Optional.ofNullable(reqForm058.getFirstVisitDate()).ifPresent(existedForm058::setFirstVisitDate);
        Optional.ofNullable(reqForm058.getDiseaseDate()).ifPresent(existedForm058::setDiagnosisDate);
        Optional.ofNullable(reqForm058.getVisitDate()).ifPresent(existedForm058::setVisitDate);
        Optional.ofNullable(reqForm058.getDocSendDate()).ifPresent(existedForm058::setDocSendDate);
        Optional.ofNullable(reqForm058.getSenderOrganizationId()).ifPresent(orgId -> existedForm058.setSenderOrganization(organizationService.findById(orgId).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found for sender organization."))));
        Optional.ofNullable(reqForm058.getReceiverOrganizationId()).ifPresent(orgId -> existedForm058.setReceiverOrganization(organizationService.findById(orgId).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found for receiver organization."))));
        Optional.ofNullable(reqForm058.getLocation()).ifPresent(reqLocation -> {
            Location exsistingLocation = existedForm058.getLocation();
            if (exsistingLocation != null) {
                Optional.ofNullable(reqLocation.getLatitude()).ifPresent(exsistingLocation::setLatitude);
                Optional.ofNullable(reqLocation.getLongitude()).ifPresent(exsistingLocation::setLongitude);
                Optional.ofNullable(reqLocation.getLocation()).ifPresent(exsistingLocation::setLocation);
            } else existedForm058.setLocation(Location.builder()
                    .latitude(reqLocation.getLatitude())
                    .longitude(reqLocation.getLongitude())
                    .location(reqLocation.getLocation())
                    .build());
        });
        Optional.ofNullable(reqForm058.getDiseasePlaceCode()).ifPresent(code -> existedForm058.setDiseasePlace(mapCatalog(CatalogType.diseasePlace, code)));
        Optional.ofNullable(reqForm058.getDiseaseCause()).ifPresent(existedForm058::setDiseaseCause);
        Optional.ofNullable(reqForm058.getEpidemicMeasures()).ifPresent(existedForm058::setEpidemicMeasures);
        Optional.ofNullable(reqForm058.getJournalFormCode()).ifPresent(form -> existedForm058.setJournalForm(mapCatalog(CatalogType.formType, form)));
        Optional.ofNullable(reqForm058.getComment()).ifPresent(existedForm058::setComment);
        Optional.ofNullable(reqForm058.getNotifierFullName()).ifPresent(existedForm058::setNotifierFullName);

        form058Repository.save(existedForm058);
        return new ApiResponse<>(true, "Success updated.", existedForm058);
    }

    @Override
    public ApiResponsePage getFilteredForm058(int page, int size, User user, String url) {
        Specification<Form058> spec = getFilteredSpecification(user.getCurrentOrganization().getUuid(), null);
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, form058Page, url);

    }

    @Transactional
    @Override
    public ApiResponse<Form058> assignCard(ReqForm058AssignCard assignCard, User user) {
        Form058 form058 = form058Repository.findById(assignCard.getForm058Id()).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found by id: " + assignCard.getForm058Id()));

        List<Card> cards = assignCard.getCardType().stream()
                .map(cardType -> {
                    Card card = createCard(cardType);
                    card.setCardType(cardType);
                    card.setCreatedBy(user.getId());
                    card.setUpdatedBy(user.getId());
                    card.setForm058(form058);
                    return card;
                })
                .toList();
        form058.getCards().clear();
        form058.getCards().addAll(cards);
//        form058.setCards(cards);
        form058.setStatus(DocumentStatus.CARD_ATTACHED);
        return new ApiResponse<>(true, "Successfully assigned.", form058Repository.save(form058));
    }

    @Override
    public ApiResponse<Form058> getById(long id) {
        return new ApiResponse<>(true, "Success",
                form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found.")));
    }

    @Override
    public ApiResponse<?> changeReceivedOrd(Long formId, Long orgId) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found."));
        Organization organization = organizationService.findById(orgId).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
        form058.setReceiverOrganization(organization);
        form058.setStatus(DocumentStatus.SENT);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Override
    public ApiResponse<?> cancel(Long formId) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found."));
        form058.setStatus(DocumentStatus.CANCELED);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Override
    public ApiResponsePage getReceivedAll(int page, int size, User user, FilterType filterType, String fullURL) {
        Specification<Form058> spec = getFilteredSpecification(user.getCurrentOrganization().getUuid(), filterType);
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, form058Page, fullURL);
    }

    @Transactional
    @Override
    public ApiResponse<?> deleteById(Long formId) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found."));
        try {
            journal060Repository.deleteByForm058(form058);
            cardRepository.removeForm058References(form058.getId());
            form058Repository.deleteById(formId);

        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
        return new ApiResponse<>(true, "Successfully delete.", null);
    }

    public static Specification<Form058> getFilteredSpecification(
            UUID currentUserOrgId,
            FilterType filterType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUserOrgId != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdOrgId"), currentUserOrgId));
            }
            if (filterType != null && filterType.equals(FilterType.RECEIVED)) {
                predicates.add(criteriaBuilder.equal(root.get("receiverOrganization").get("uuid"), currentUserOrgId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
