package uz.uzinfocom.isemid.features.form058.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.factory.CardFactory;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.utils.CardMessageUtil;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.mappers.Form058Mapper;
import uz.uzinfocom.isemid.features.form058.mappers.Form058ResponseAdapter;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.Location;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.form058.specifications.Form058Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.patient.models.Address;
import uz.uzinfocom.isemid.features.patient.models.Identifier;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    private final Form058Repository form058Repository;
    private final CardFactory cardFactory;
    private final UserService userService;
    private final Form058Mapper form058Mapper;
    private final OrganizationRepository organizationRepository;
    private final Form058ResponseAdapter form058ResponseAdapter;
    private final CardRepository cardRepository;


    @Override
    public ApiResponse<Form058Response> create(Form058Request request, User user) {
        Patient patient = Patient.builder()
                .firstName(request.getPatient().getFirstName())
                .lastName(request.getPatient().getLastName())
                .middleName(request.getPatient().getMiddleName())
                .birthDate(request.getPatient().getBirthDate())
                .genderCode(request.getPatient().getGender().name())
                .workPlace(request.getPatient().getWorkPlace())
                .workPlaceStateCode(request.getPatient().getWorkPlaceStateCode())
                .workPlaceCityCode(request.getPatient().getWorkPlaceCityCode())
                .residentialStatusCode(request.getPatient().getResidentialStatus().name())
                .categoryCode(request.getPatient().getCategoryCode())
                .professionCode(request.getPatient().getProfessionCode())
                .populationTypeCode(request.getPatient().getPopulationType().name())
                .maritalStatusCode(request.getPatient().getMaritalStatus().name())
                .build();

        List<Identifier> identifiers = request.getPatient().getIdentifiers().stream().map(reqIdentifier -> {
            Identifier identifier = Identifier.builder()
                    .value(reqIdentifier.getValue())
                    .typeCode(reqIdentifier.getTypeCode().name())
                    .periodEnd(reqIdentifier.getPeriodEnd())
                    .periodStart(reqIdentifier.getPeriodStart())
                    .build();
            identifier.setPatient(patient);
            return identifier;
        }).toList();
        patient.setIdentifiers(identifiers);

        List<Address> addresses = request.getPatient().getAddresses().stream().map(reqAddress -> {
            Address address = Address.builder()
                    .stateCode(reqAddress.getStateCode())
                    .cityCode(reqAddress.getCityCode())
                    .neighborhoodCode(reqAddress.getNeighborhood())
                    .streetAddress(reqAddress.getStreetAddress())
                    .houseNumber(reqAddress.getHouseNumber())
                    .apartmentNumber(reqAddress.getApartmentNumber())
                    .type(reqAddress.getType())
                    .build();
            address.setPatient(patient);
            return address;
        }).toList();
        patient.setAddresses(addresses);
        Long senderOrganizationId = organizationRepository.findByUuid(request.getSenderOrganizationId())
                .map(Organization::getId)
                .orElse(null);

        Form058 form058 = Form058.builder()
                .mkb10Code(request.getMkb10Code())
                .mkb10Name(request.getMkb10Name())
                .finalMkb10Code(request.getFinalMkb10Code())
                .finalMkb10Name(request.getFinalMkb10Name())
                .mkb10UsageLimit(request.getMkb10UsageLimit())
                .status(FormStatus.SENT)
                .patient(patient)
                .labConfirmation(request.getLabConfirmation())
                .hospitalPlaceId(request.getHospitalPlaceId())
                .admissionDate(request.getAdmissionDate())
                .diseaseDate(request.getDiseaseDate())
                .firstVisitDate(request.getFirstVisitDate())
                .diagnosisDate(request.getDiagnosisDate())
                .visitDate(request.getVisitDate())
                .senderOrganizationId(senderOrganizationId)
                .receiverOrganizationId(request.getReceiverOrganizationId())
                .location(Location.builder()
                        .latitude(request.getLocation().getLatitude())
                        .longitude(request.getLocation().getLongitude())
                        .location(request.getLocation().getLocation())
                        .build())
                .diseasePlaceCode(request.getDiseasePlaceCode())
                .diseaseCause(request.getDiseaseCause())
                .epidemicMeasures(request.getEpidemicMeasures())
                .journalFormCode(request.getJournalFormCode())
                .notifierFullName(request.getNotifierFullName())
                .comment(request.getComment())
                .initialReportDateTime(request.getInitialReportDateTime())
                .docSendDate(LocalDateTime.now())
                .build();

        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> update(Form058Request request, Long id, User user) {
        Form058 existedForm058 = form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        Optional.ofNullable(request.getMkb10Code()).ifPresent(existedForm058::setMkb10Code);
        Optional.ofNullable(request.getMkb10Name()).ifPresent(existedForm058::setMkb10Name);
        Optional.ofNullable(request.getMkb10UsageLimit()).ifPresent(existedForm058::setMkb10UsageLimit);
        Optional.ofNullable(request.getPatient()).ifPresent(reqPatient -> {
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
                                            .equals(existingIdentifier.getTypeCode()))
                                    .findFirst()
                                    .ifPresent(reqIdentifier -> {
                                        existingIdentifier.setValue(reqIdentifier.getValue());
                                        existingIdentifier.setPeriodStart(reqIdentifier.getPeriodStart());
                                        existingIdentifier.setPeriodEnd(reqIdentifier.getPeriodEnd());
                                    })
                    );

                    reqIdentifiers.stream()
                            .filter(reqIdentifier -> exsistingIdentifiers.stream()
                                    .noneMatch(existingIdentifier -> existingIdentifier.getTypeCode()
                                            .equals(reqIdentifier.getTypeCode().name())))
                            .forEach(reqIdentifier -> {
                                Identifier newIdentifier = Identifier.builder()
                                        .patient(patient)
                                        .typeCode(reqIdentifier.getTypeCode().name())
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
                Optional.ofNullable(reqPatient.getGender()).ifPresent(reqGen -> patient.setGenderCode(reqGen.name()));
                Optional.ofNullable(reqPatient.getPhoneNumber()).ifPresent(patient::setPhoneNumber);
                Optional.ofNullable(reqPatient.getKinshipDegree()).ifPresent(patient::setKinshipDegree);
                Optional.ofNullable(reqPatient.getKinshipFullName()).ifPresent(patient::setKinshipFullName);
                Optional.ofNullable(reqPatient.getResidentialStatus()).ifPresent(reqResidentialStatus -> patient.setResidentialStatusCode(reqResidentialStatus.name()));
                Optional.ofNullable(reqPatient.getMaritalStatus()).ifPresent(reqMaritalStatus -> patient.setMaritalStatusCode(reqMaritalStatus.name()));
                Optional.ofNullable(reqPatient.getPopulationType()).ifPresent(reqPopulationType -> patient.setPopulationTypeCode(reqPopulationType.name()));
                Optional.ofNullable(reqPatient.getCategoryCode()).ifPresent(patient::setCategoryCode);
                Optional.ofNullable(reqPatient.getProfessionCode()).ifPresent(patient::setProfessionCode);
                Optional.ofNullable(reqPatient.getWorkPlace()).ifPresent(patient::setWorkPlace);
                Optional.ofNullable(reqPatient.getWorkPlaceStateCode()).ifPresent(patient::setWorkPlaceStateCode);
                Optional.ofNullable(reqPatient.getWorkPlaceCityCode()).ifPresent(patient::setWorkPlaceCityCode);
                Optional.ofNullable(reqPatient.getAddresses()).ifPresent(reqAddresses -> {

                    List<Address> existingAddresses = patient.getAddresses();

                    existingAddresses.forEach(existingAddress ->
                            reqAddresses.stream()
                                    .filter(reqAddress -> reqAddress.getType()
                                            .equals(existingAddress.getType()))
                                    .findFirst()
                                    .ifPresent(reqAddress -> {
                                        existingAddress.setStateCode(reqAddress.getStateCode());
                                        existingAddress.setCityCode(reqAddress.getCityCode());
                                        existingAddress.setNeighborhoodCode(reqAddress.getNeighborhood());
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
                                Address newAddress = Address.builder()
                                        .patient(patient)
                                        .type(reqAddress.getType())
                                        .stateCode(reqAddress.getStateCode())
                                        .cityCode(reqAddress.getCityCode())
                                        .neighborhoodCode(reqAddress.getNeighborhood())
                                        .streetAddress(reqAddress.getStreetAddress())
                                        .houseNumber(reqAddress.getHouseNumber())
                                        .apartmentNumber(reqAddress.getApartmentNumber())
                                        .build();

                                patient.getAddresses().add(newAddress);
                            });
                });
            }
        });

        Optional.ofNullable(request.getLabConfirmation()).ifPresent(existedForm058::setLabConfirmation);

        Optional.ofNullable(request.getHospitalPlaceId()).ifPresent(existedForm058::setHospitalPlaceId);

        Optional.ofNullable(request.getAdmissionDate()).ifPresent(existedForm058::setAdmissionDate);
        Optional.ofNullable(request.getDiagnosisDate()).ifPresent(existedForm058::setDiagnosisDate);
        Optional.ofNullable(request.getFirstVisitDate()).ifPresent(existedForm058::setFirstVisitDate);
        Optional.ofNullable(request.getDiseaseDate()).ifPresent(existedForm058::setDiagnosisDate);
        Optional.ofNullable(request.getVisitDate()).ifPresent(existedForm058::setVisitDate);
        Optional.ofNullable(request.getDocSendDate()).ifPresent(existedForm058::setDocSendDate);
        Optional.ofNullable(request.getSenderOrganizationId()).ifPresent(uuid -> existedForm058.setSenderOrganizationId(organizationRepository.findByUuid(uuid).map(Organization::getId).orElse(null)));
        Optional.ofNullable(request.getReceiverOrganizationId()).ifPresent(existedForm058::setReceiverOrganizationId);
        Optional.ofNullable(request.getLocation()).ifPresent(reqLocation -> {
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
        Optional.ofNullable(request.getDiseasePlaceCode()).ifPresent(existedForm058::setDiseasePlaceCode);
        Optional.ofNullable(request.getDiseaseCause()).ifPresent(existedForm058::setDiseaseCause);
        Optional.ofNullable(request.getEpidemicMeasures()).ifPresent(existedForm058::setEpidemicMeasures);
        Optional.ofNullable(request.getJournalFormCode()).ifPresent(existedForm058::setJournalFormCode);
        Optional.ofNullable(request.getComment()).ifPresent(existedForm058::setComment);
        Optional.ofNullable(request.getNotifierFullName()).ifPresent(existedForm058::setNotifierFullName);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, form058Mapper.toResponse(form058Repository.save(existedForm058)));
    }

    @Override
    public ApiResponsePage getAllFilteredForm058(int page, int size, LocalDate fromDate, LocalDate toDate, User user, String url, FilterType filterType) {
        Specification<Form058> spec = Form058Specification.getFilteredSpecification(fromDate, toDate, user, filterType);
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));

        Page<Form058Response> responsePage = form058Page.map(form -> {
            Form058Response base = form058Mapper.toResponse(form);
            return form058ResponseAdapter.adapt(base, form, filterType);
        });
        return createApiResponsePage(page, responsePage, url);

    }

    private Form058 getForm058ById(Long id) {
        return form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Form 058", id)));
    }

    @Override
    public ApiResponse<Form058Response> getById(long id) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS,
                form058Mapper.toResponse(form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND))));
    }

    @Override
    public ApiResponse<Form058Response> assignCard(Form058AssignCardRequest request, User user) {
        if (request.getAssignUserIds() == null || request.getAssignUserIds().isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Список назначенных пользователей пуст.");
        }

        if (request.getCardType() == null || request.getCardType().isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Требуются типы карт.");
        }
        Form058 form058 = this.getForm058ById(request.getForm058Id());

        Map<Long, User> userMap = userService.getAllByIds(request.getAssignUserIds()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Card> cards = buildCards(request.getCardType(), userMap, form058, user);
        form058.setCardByFullName(user.getFullName());
        form058.getCards().addAll(cards);
        form058.setStatus(FormStatus.CARD_LINKED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    private List<Card> buildCards(List<CardType> cardTypes, Map<Long, User> userMap, Form058 form058, User assignedBy) {
        return cardTypes.stream()
                .map(cardType -> {
                    Card card = cardFactory.createCard(cardType);
                    card.setUsers(new HashSet<>(userMap.values()));
                    card.setStatus(CardStatus.NEW);
                    card.setCardType(cardType);
                    card.setAssignedById(assignedBy.getId());
                    card.setForm058(form058);
                    return card;
                }).toList();
    }

    @Override
    public ApiResponse<Form058Response> changeReceivedOrd(Long formId, Form058UpdateReceivedOrg updateReceivedOrg) {
        Form058 form058 = this.getForm058ById(formId);
        form058.getPatient().setWorkPlaceOrgId(updateReceivedOrg.getWorkPlaceOrgId());
        form058.getPatient().setWorkPlaceStateCode(updateReceivedOrg.getWorkPlaceStateCode());
        form058.getPatient().setWorkPlaceCityCode(updateReceivedOrg.getWorkPlaceCityCode());
        form058.setReceiverOrganizationId(updateReceivedOrg.getReceivedOrgId());
        form058.getPatient().getAddresses().stream()
                .filter(address -> address.getType().equals(AddressType.TEMPORARY))
                .findFirst()
                .ifPresent(address -> {
                    address.setStateCode(updateReceivedOrg.getAddressStateCode());
                    address.setCityCode(updateReceivedOrg.getAddressCityCode());
                    address.setNeighborhoodCode(updateReceivedOrg.getAddressNeighborhoodCode());
                    address.setStreetAddress(updateReceivedOrg.getStreetAddress());
                    address.setHouseNumber(updateReceivedOrg.getHouseNumber());
                    address.setApartmentNumber(updateReceivedOrg.getApartmentNumber());
                });
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> cancel(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
        form058.setStatus(FormStatus.CANCELED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> approveForm(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
        boolean allCardsApproved = form058.getCards().stream()
                .allMatch(card -> card.getStatus().equals(CardStatus.APPROVED));
        if (!allCardsApproved) {
            throw new ApiException(BAD_REQUEST, CardMessageUtil.CARDS_NOT_APPROVED);
        }
        form058.setStatus(FormStatus.APPROVED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<Form058Response> notApprove(Long formId, Form058NotApproveRequest notApprove) {
        Form058 form058 = this.getForm058ById(formId);
        form058.setNotApproveComment(notApprove.getComment());
        form058.setStatus(FormStatus.NOT_APPROVED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }

    @Override
    public ApiResponse<?> deleteById(Long formId) {
        Form058 form058 = this.getForm058ById(formId);
        List<Card> cards = form058.getCards();

        for (Card card : cards) {
            if (card instanceof Card161 card161) {
                if (card161.getInfectionSourceDetail() != null) {
                    card161.setInfectionSourceDetail(null);
                    cardRepository.save(card161);
                }
            }
        }
        cardRepository.flush();
        try {
            form058Repository.delete(form058);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return new ApiResponse<>(true, MessageUtil.DELETED_SUCCESS, null);
    }

    @Override
    public ApiResponse<?> changeDiagnosis(Long formId, Form058ChangeDiagnosisRequest changeDiagnosis) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, formId)));
        if (!form058.getStatus().equals(FormStatus.NOT_APPROVED))
            throw new ApiException(BAD_REQUEST, MessageUtil.CANNOT_CHANGE_DIAGNOSIS);
        form058.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        form058.setFinalMkb10Name(changeDiagnosis.getFinalMkb10Name());
        form058.setDiagnosisDate(LocalDateTime.now());
        form058.setStatus(FormStatus.APPROVED);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, form058Mapper.toResponse(form058Repository.save(form058)));
    }
}
