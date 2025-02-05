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
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058AssignCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.repositories.Journal060Repository;
import uz.uzinfocom.isemid.services.Form058Service;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.UserService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    final Form058Repository form058Repository;
    final ModelMapper modelMapper;
    final UserService userService;
    final CatalogService catalogService;
    final OrganizationService organizationService;
    final Journal060Repository journal060Repository;

    @Override
    public ApiResponse<Form058> createForm058(ReqForm058 reqForm058, User user) {
        Patient patient = Patient.builder()
                .firstName(reqForm058.getPatient().getFirstName())
                .lastName(reqForm058.getPatient().getLastName())
                .middleName(reqForm058.getPatient().getMiddleName())
                .birthDate(reqForm058.getPatient().getBirthDate())
                .gender(reqForm058.getPatient().getGender())
                .workPlace(reqForm058.getPatient().getWorkPlace())
                .residentialStatus(reqForm058.getPatient().getResidentialStatus())
                .maritalStatus(reqForm058.getPatient().getMaritalStatus())
                .build();

        List<Identifier> identifiers = reqForm058.getPatient().getIdentifiers().stream().map(reqIdentifier -> {
            Identifier identifier = Identifier.builder()
                    .value(reqIdentifier.getValue())
                    .typeCode(reqIdentifier.getTypeCode())
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
                    .street(catalogService.findByTypeAndCode(CatalogType.street, reqAddress.getStreetAddress()).orElse(null))
                    .neighborhood(reqAddress.getNeighborhood())
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
                .diseasePlace(catalogService.findByTypeAndCode(CatalogType.diseasePlace, reqForm058.getJournalFormCode()).orElse(null))
                .diseaseCause(reqForm058.getDiseaseCause())
                .epidemicMeasures(reqForm058.getEpidemicMeasures())
                .journalForm(catalogService.findByTypeAndCode(CatalogType.formType, reqForm058.getJournalFormCode()).orElse(null))
                .notifierFullName(reqForm058.getNotifierFullName())
                .comment(reqForm058.getComment())
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
        return new ApiResponse<>(true, "Success", null);
    }

    @Override
    public ApiResponsePage getFilteredForm058(int page, int size, User user, String url) {
        Specification<Form058> spec = getFilteredSpecification(user.getCurrentOrganization().getUuid());
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, form058Page, url);

    }

    @Transactional
    @Override
    public ApiResponse<Form058> assignCard(ReqForm058AssignCard assignCard, User user) {
        Form058 form058 = form058Repository.findById(assignCard.getForm058Id()).orElseThrow(() -> new ApiException(NOT_FOUND, "Form 058 not found by id: " + assignCard.getForm058Id()));
        Card card = createCard(assignCard.getCardType());
        card.setCardType(assignCard.getCardType());
        card.setCreatedBy(user.getId());
        card.setUpdatedBy(user.getId());
        form058.setCard(card);
        return new ApiResponse<>(true, "Successfully assigned.", form058Repository.save(form058));
    }

    public static Specification<Form058> getFilteredSpecification(
            UUID currentUserOrgId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUserOrgId != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdOrgId"), currentUserOrgId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
