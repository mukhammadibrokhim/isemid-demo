package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.*;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm058;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Form058ServiceImpl implements Form058Service {
    final Form058Repository form058Repository;
    final ModelMapper modelMapper;

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
                .notifierFullName(user.getFirstName())
                .senderOrganization(reqForm058.getSenderOrganization())
                .receiverOrganization(reqForm058.getReceiverOrganization())
                .journalForm(reqForm058.getJournalForm())
                .visitDate(reqForm058.getVisitDate())
                .patient(patient)
                .build();

        form058Repository.save(form058);
        return new ApiResponse<>(true, "Форма 058 успешно создана.", form058);
    }


    @Override
    public ApiResponse<Form058> updateForm058(ReqForm058 reqForm058, Long id, User user) {

        return new ApiResponse<>(true, "", null);
    }

    @Override
    public ApiResponsePage getFilteredForm058(int page, int size, User user, String url) {
        Specification<Form058> spec = getFilteredSpecification(user.getCurrentOrganization().getUuid());
        Page<Form058> form058Page = form058Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, form058Page, url);

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
