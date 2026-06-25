package uz.uzinfocom.app.modules.form058.application.query.projection;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

import java.util.List;
import java.util.UUID;

public interface Form058TableProjection {
    Long getId();

    UUID getUuid();

    FormStatus getStatus();

    String getSource();

    Form058DiagnosisInfoProjection getDiagnosisInfo();

    Long getSenderOrganizationId();

    PatientProjection getPatient();

    interface PatientProjection {
        Long getId();

        String getFirstName();

        String getLastName();

        String getMiddleName();

        List<PatientAddressProjection> getAddresses();
    }

    interface PatientAddressProjection {
        Long getId();

        AddressType getType();

        String getRegionCode();

        String getDistrictCode();

        String getNeighborhoodCode();

        String getStreetAddress();

        String getHouseNumber();

        String getApartmentNumber();
    }
}
