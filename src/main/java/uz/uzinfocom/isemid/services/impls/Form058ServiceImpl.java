package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.entities.enums.AddressType;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.enums.FilterType;
import uz.uzinfocom.isemid.models.entities.enums.FormStatus;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.form.*;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.repositories.Journal060Repository;
import uz.uzinfocom.isemid.repositories.OrganizationRepository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.Form058Service;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.UserService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;
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
    final OrganizationRepository organizationRepository;

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
                .finalMkb10Code(reqForm058.getFinalMkb10Code())
                .finalMkb10Name(reqForm058.getFinalMkb10Name())
                .mkb10UsageLimit(reqForm058.getMkb10UsageLimit())
                .status(FormStatus.SENT)
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
                Optional.ofNullable(reqPatient.getGender()).ifPresent(reqGen -> patient.setGender(mapCatalog(CatalogType.gender, reqGen.name())));
                Optional.ofNullable(reqPatient.getPhoneNumber()).ifPresent(patient::setPhoneNumber);
                Optional.ofNullable(reqPatient.getKinshipDegree()).ifPresent(patient::setKinshipDegree);
                Optional.ofNullable(reqPatient.getKinshipFullName()).ifPresent(patient::setKinshipFullName);
                Optional.ofNullable(reqPatient.getResidentialStatus()).ifPresent(reqResidentialStatus -> patient.setResidentialStatus(mapCatalog(CatalogType.residentialStatus, reqResidentialStatus.name())));
                Optional.ofNullable(reqPatient.getMaritalStatus()).ifPresent(reqMaritalStatus -> patient.setResidentialStatus(mapCatalog(CatalogType.maritalStatus, reqMaritalStatus.name())));
                Optional.ofNullable(reqPatient.getPopulationType()).ifPresent(reqPopulationType -> patient.setPopulationType(mapCatalog(CatalogType.populationType, reqPopulationType.name())));
                Optional.ofNullable(reqPatient.getCategoryCode()).ifPresent(reqCategoryCode -> patient.setCategory(mapCatalog(CatalogType.category, reqCategoryCode)));
                Optional.ofNullable(reqPatient.getProfessionCode()).ifPresent(code -> patient.setProfession(mapCatalog(CatalogType.profession, code)));
                Optional.ofNullable(reqPatient.getWorkPlace()).ifPresent(patient::setWorkPlace);
                Optional.ofNullable(reqPatient.getWorkPlaceStateCode()).ifPresent(code -> patient.setWorkPlaceState(mapCatalog(CatalogType.state, code)));
                Optional.ofNullable(reqPatient.getWorkPlaceCityCode()).ifPresent(code -> patient.setWorkPlaceCity(mapCatalog(CatalogType.city, code)));
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
    public ApiResponsePage getFilteredForm058(int page, int size, User user, String url, FilterType filterType) {
        Specification<Form058> spec = getFilteredSpecification(user.getCurrentOrganization().getUuid(), filterType);
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, form058Page, url);

    }

    @Override
    public ApiResponse<Form058> assignCard(ReqForm058AssignCard assignCard, User user) {
        Form058 form058 = form058Repository.findById(assignCard.getForm058Id())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found by id: " + assignCard.getForm058Id()));

        Map<Long, User> userMap = userService.getAllByIds(assignCard.getAssignUserIds()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Card> cards = assignCard.getCardType().stream()
                .map(cardType -> {
                    Card card = createCard(cardType);
                    card.setCardType(cardType);
                    card.setUsers(new HashSet<>(userMap.values()));
                    card.setCreatedBy(user.getId());
                    card.setUpdatedBy(user.getId());
                    card.setStatus(CardStatus.NEW);
                    card.setAssignedById(user.getId());
                    card.setForm058(form058);
                    return card;
                })
                .toList();
        form058.setCardByFullName(user.getFullName());
        form058.getCards().clear();
        form058.getCards().addAll(cards);
        form058.setStatus(FormStatus.CARD_LINKED);
        return new ApiResponse<>(true, "Successfully assigned.", form058Repository.save(form058));
    }

    @Override
    public ApiResponse<Form058> getById(long id) {
        return new ApiResponse<>(true, "Success",
                form058Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found.")));
    }

    @Override
    public ApiResponse<?> changeReceivedOrd(Long formId, ReqForm058OrgUpdate reqForm058OrgUpdate) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found."));

        Patient patient = form058.getPatient();
        Optional.ofNullable(reqForm058OrgUpdate.getWorkPlaceStateCode())
                .ifPresent(code -> patient.setWorkPlaceState(mapCatalog(CatalogType.state, code)));

        Optional.ofNullable(reqForm058OrgUpdate.getWorkPlaceCityCode())
                .ifPresent(code -> patient.setWorkPlaceCity(mapCatalog(CatalogType.city, code)));
        Optional.ofNullable(reqForm058OrgUpdate.getWorkPlaceRecOrgId()).ifPresent(id -> patient.setWorkPlaceOrg(organizationService.findById(reqForm058OrgUpdate.getWorkPlaceRecOrgId())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Work place organization not found."))));

        patient.getAddresses().stream()
                .filter(address -> address.getType().equals(AddressType.TEMPORARY))
                .findFirst()
                .ifPresent(address -> {
                    Optional.ofNullable(reqForm058OrgUpdate.getAddressStateCode())
                            .ifPresent(code -> address.setState(mapCatalog(CatalogType.state, code)));

                    Optional.ofNullable(reqForm058OrgUpdate.getAddressCityCode())
                            .ifPresent(code -> address.setCity(mapCatalog(CatalogType.city, code)));

                    Optional.ofNullable(reqForm058OrgUpdate.getAddressNeighborhoodCode())
                            .ifPresent(code -> address.setNeighborhood(mapCatalog(CatalogType.neighborhood, code)));

                    Optional.ofNullable(reqForm058OrgUpdate.getStreetAddress())
                            .ifPresent(address::setStreetAddress);

                    Optional.ofNullable(reqForm058OrgUpdate.getHouseNumber())
                            .ifPresent(address::setHouseNumber);

                    Optional.ofNullable(reqForm058OrgUpdate.getApartmentNumber())
                            .ifPresent(address::setApartmentNumber);
                });

        Organization organization = organizationService.findById(reqForm058OrgUpdate.getReceivedOrgId()).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
        form058.setReceiverOrganization(organization);
        form058.setStatus(FormStatus.SENT);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Override
    public ApiResponse<?> cancel(Long formId) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found."));
        form058.setStatus(FormStatus.CANCELED);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
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

    @Override
    public ApiResponse<?> approveForm(Long formId) {
        Form058 form058 = form058Repository.findById(formId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Form058 not found."));

        List<Card> form058Cards = form058.getCards();

        boolean allCardsApproved = form058Cards.stream()
                .allMatch(card -> card.getStatus().equals(CardStatus.APPROVED));

        if (!allCardsApproved) {
            throw new ApiException(BAD_REQUEST, "Not all cards are approved.");
        }

        form058.setStatus(FormStatus.APPROVED);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Override
    public ApiResponse<?> notApprove(Long formId, ReqForm058NotApprove notApprove) {
        Form058 form058 = form058Repository.findById(formId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Form058 not found."));
        form058.setNotApproveComment(notApprove.getComment());
        form058.setStatus(FormStatus.NOT_APPROVED);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Transactional
    @Override
    public ApiResponse<?> changeDiagnosis(Long formId, ReqForm058ChangeDiagnosis changeDiagnosis) {
        Form058 form058 = form058Repository.findById(formId).orElseThrow(() -> new ApiException(NOT_FOUND, "Form058 not found."));
        if (!form058.getStatus().equals(FormStatus.NOT_APPROVED))
            throw new ApiException(BAD_REQUEST, "Cannot change diagnosis.");
        form058.setFinalMkb10Code(changeDiagnosis.getFinalMkb10Code());
        form058.setFinalMkb10Name(changeDiagnosis.getFinalMkb10name());
        form058.setDiagnosisDate(changeDiagnosis.getDiseaseDate());
        form058.setStatus(FormStatus.APPROVED);
        return new ApiResponse<>(true, "Success", form058Repository.save(form058));
    }

    @Override
    public ByteArrayInputStream exportForm058ToExcel(User currentUser, FilterType filterType) {
        Specification<Form058> spec = getFilteredSpecification(currentUser.getCurrentOrganization().getUuid(), filterType);
        List<Form058> formList = form058Repository.findAll(spec);

        String[] columns = {
                "№", "Бирламчи тезкор хабар-нома", "Муассаса", "Бемор", "Ёши", "Манзил", "Иш жойи",
                "Касал бўлган кун", "Биринчи мурожаат", "Ётқизилган жойи", "Ташхис", "Ўзгарган ташхис",
                "Эпид. текширув", "Ҳабар", "Лаб. натижа", "Эслатма"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Юқумли касалликлар журнали");
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headerLines = {
                    "Ўзбекистон Республикаси",
                    "Соғлиқни сақлаш Вазирлиги",
                    "______________________",
                    currentUser.getCurrentOrganization().getName(),
                    "Ўзбекистон Республикаси Соғлиқни сақлаш вазирининг 2020 йил 31 декабрдаги 363-сонли буйруғи билан тасдиқланган",
                    "060- рақамли тиббий ҳужжат шакли",
                    "ЮҚУМЛИ  КАСАЛЛИКЛАРНИ  ҚАЙД ҚИЛИШ ЖУРНАЛИ",
                    "Бошланган сана   «_____» ___________________________20____ йил",
                    "Эслатма: 13 – 14 графалар фақат санитария -эпидемиология назорат марказларида тўлдирилади.",
                    "Тугалланган сана  «_______» _________________________20____ йил"
            };

            for (int i = 0; i < headerLines.length; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(headerLines[i]);
                cell.setCellStyle(centerStyle);
                sheet.addMergedRegion(new CellRangeAddress(i, i, 0, columns.length - 1));
            }

            // Ustun sarlavhalari
            Row headerRow = sheet.createRow(10);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Ma’lumotlar
            int rowNum = 11;
            int index = 1;
            for (Form058 form058 : formList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(index++);
                row.createCell(1).setCellValue(""); // телефон хабар
                row.createCell(2).setCellValue(form058.getCreatedOrg().getName());
                row.createCell(3).setCellValue(form058.getPatient().getFirstName() + " " + form058.getPatient().getLastName());
                row.createCell(4).setCellValue(calculateAge(form058.getPatient().getBirthDate()));
                row.createCell(5).setCellValue("form058.getPatient()"); // Fixed method call
                row.createCell(6).setCellValue(form058.getPatient().getFirstName()); // Fixed method call
                row.createCell(7).setCellValue("getString(form058.getIllnessStartDate())"); // Fixed method call
                row.createCell(8).setCellValue("getString(form058.getFirstVisitDate())"); // Fixed method call
                row.createCell(9).setCellValue("getString(form058.getHospitalizationDate())"); // Fixed method call
                row.createCell(10).setCellValue("form058.getDiagnosis()");
                row.createCell(11).setCellValue("form058.getRevisedDiagnosis()");
                row.createCell(12).setCellValue("getString(form058.getEpidemiologicalInspectionDate())"); // Fixed method call
                row.createCell(13).setCellValue("form058.getReportedTo()");
                row.createCell(14).setCellValue("form058.getLabResult()");
                row.createCell(15).setCellValue("form058.getNote()");
            }

            // Ustunlarni avto-o‘lchamga sozlash
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Excel export error", e);
        }
    }

    private String getString(Date date) {
        if (date != null) {
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        return "";
    }


    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }


    private String getString(LocalDate date) {
        return date != null ? date.toString() : "";
    }

    public static Specification<Form058> getFilteredSpecification(
            UUID currentUserOrgId,
            FilterType filterType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUserOrgId != null) {
                if (filterType == FilterType.RECEIVED) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("receiverOrganization").get("uuid"), currentUserOrgId));
                } else {
                    Expression<UUID> createdOrgUuid = root.get("createdOrgUuid");

                    Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                    Join<Patient, Organization> workplaceJoin = patientJoin.join("workPlaceOrg", JoinType.LEFT);

                    Expression<UUID> workplaceOrgUuid = workplaceJoin.get("uuid");

                    Predicate createdMatches = criteriaBuilder.equal(createdOrgUuid, currentUserOrgId);
                    Predicate workplaceMatches = criteriaBuilder.equal(workplaceOrgUuid, currentUserOrgId);
                    Predicate eitherMatches = criteriaBuilder.or(createdMatches, workplaceMatches);

                    predicates.add(eitherMatches);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
