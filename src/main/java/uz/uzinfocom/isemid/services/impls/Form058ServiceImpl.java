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
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058AssignCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058Service;
import uz.uzinfocom.isemid.services.UserService;

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
                    .stateCode(reqAddress.getStateCode())
                    .cityCode(reqAddress.getCityCode())
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
                .admissionDate(reqForm058.getAdmissionDate())
                .comment(reqForm058.getComment())
                .diagnosisDate(reqForm058.getDiagnosisDate())
                .diseaseCause(reqForm058.getDiseaseCause())
                .diseaseDate(reqForm058.getDiseaseDate())
                .mkb10Code(reqForm058.getMkb10Code())
                .mkb10Name(reqForm058.getMkb10Name())
                .epidemicMeasures(reqForm058.getEpidemicMeasures())
                .firstVisitDate(reqForm058.getFirstVisitDate())
                .hospitalPlace(reqForm058.getHospitalPlace())
                .senderOrganization(reqForm058.getSenderOrganization())
                .receiverOrganization(reqForm058.getReceiverOrganization())
                .journalForm(reqForm058.getJournalForm())
                .deceaseLocation(reqForm058.getDeceaseLocation())
                .card(reqForm058.getCard())
                .reporter(reporter)
                .visitDate(reqForm058.getVisitDate())
                .patient(patient)
                .build();
        return new ApiResponse<>(true, "Форма 058 успешно создана.", form058Repository.save(form058));
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
