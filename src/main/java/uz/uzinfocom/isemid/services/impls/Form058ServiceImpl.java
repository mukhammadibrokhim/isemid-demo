package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.Identifier;
import uz.uzinfocom.isemid.models.entities.Patient;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm058;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058Service;

import java.util.List;

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
                .apartmentNumber(reqForm058.getPatient().getApartmentNumber())
                .birthDate(reqForm058.getPatient().getBirthDate())
                .gender(reqForm058.getPatient().getGender())
                .stateCode(reqForm058.getPatient().getStateCode())
                .cityCode(reqForm058.getPatient().getCityCode())
                .neighborhood(reqForm058.getPatient().getNeighborhood())
                .houseNumber(reqForm058.getPatient().getHouseNumber())
                .streetAddress(reqForm058.getPatient().getStreetAddress())
                .workPlace(reqForm058.getPatient().getWorkPlace())
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
}
