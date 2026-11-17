package uz.uzinfocom.app.modules.form058.application.query.mapper.helper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAddressJpaRepository;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAddressJpaRepository.PatientAddressView;
import uz.uzinfocom.app.modules.reference.application.lookup.mapper.ReferenceMappingHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fills the permanent-address columns of table rows with one bulk address query per page.
 * Addresses are deliberately not part of the table projection - see
 * {@link PatientAddressJpaRepository#findByPatientIdInAndType}.
 */
@Component
public class Form058TableAddressMapperHelper {

    private final ReferenceMappingHelper referenceMappingHelper;
    private final PatientAddressJpaRepository patientAddressRepository;

    public Form058TableAddressMapperHelper(
            ReferenceMappingHelper referenceMappingHelper,
            PatientAddressJpaRepository patientAddressRepository
    ) {
        this.referenceMappingHelper = referenceMappingHelper;
        this.patientAddressRepository = patientAddressRepository;
    }

    public List<Form058TableResponse> withPermanentAddresses(List<Form058TableResponse> rows) {
        List<Long> patientIds = rows.stream()
                .map(row -> row.patient() != null ? row.patient().id() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (patientIds.isEmpty()) {
            return rows;
        }

        Map<Long, PatientAddressView> addressByPatientId = patientAddressRepository
                .findByPatientIdInAndType(patientIds, AddressType.PERMANENT)
                .stream()
                .collect(Collectors.toMap(
                        PatientAddressView::getPatientId,
                        Function.identity(),
                        (first, second) -> first
                ));

        return rows.stream()
                .map(row -> withAddress(row, addressByPatientId))
                .toList();
    }

    private Form058TableResponse withAddress(Form058TableResponse row, Map<Long, PatientAddressView> addressByPatientId) {
        if (row.patient() == null) {
            return row;
        }

        PatientAddressView address = addressByPatientId.get(row.patient().id());

        if (address == null) {
            return row;
        }

        Form058TableResponse.PatientShortResponse patient = row.patient();

        return new Form058TableResponse(
                row.id(),
                row.uuid(),
                row.createdAt(),
                row.status(),
                row.icd10Code(),
                row.icd10Name(),
                row.source(),
                row.senderOrganizationId(),
                row.senderOrganizationName(),
                row.receiverOrganizationId(),
                row.receiverOrganizationName(),
                new Form058TableResponse.PatientShortResponse(
                        patient.id(),
                        patient.firstName(),
                        patient.lastName(),
                        patient.middleName(),
                        isBlank(address.getRegionCode()) ? null : referenceMappingHelper.regionName(address.getRegionCode()),
                        isBlank(address.getDistrictCode()) ? null : referenceMappingHelper.districtName(address.getDistrictCode()),
                        isBlank(address.getNeighborhoodCode()) ? null : referenceMappingHelper.neighborhoodName(address.getNeighborhoodCode()),
                        isBlank(address.getStreetAddress()) ? null : address.getStreetAddress(),
                        isBlank(address.getHouseNumber()) ? null : address.getHouseNumber(),
                        isBlank(address.getApartmentNumber()) ? null : address.getApartmentNumber()
                )
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
