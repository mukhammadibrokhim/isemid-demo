package uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;

import java.util.Collection;
import java.util.List;

public interface PatientAddressJpaRepository extends JpaRepository<PatientAddress, Long> {

    /**
     * Bulk "address of this type" lookup for one page of table rows. Kept out of the
     * table projection on purpose: a collection path inside a Spring Data projection
     * is joined with an inner join, which silently drops every form whose patient has
     * no address of the requested shape while {@code count(spec)} still counts it.
     */
    @Query("""
            SELECT a.patient.id AS patientId,
                   a.regionCode AS regionCode,
                   a.districtCode AS districtCode,
                   a.neighborhoodCode AS neighborhoodCode,
                   a.streetAddress AS streetAddress,
                   a.houseNumber AS houseNumber,
                   a.apartmentNumber AS apartmentNumber
            FROM PatientAddress a
            WHERE a.patient.id IN :patientIds
              AND a.type = :type
            ORDER BY a.id
            """)
    List<PatientAddressView> findByPatientIdInAndType(
            @Param("patientIds") Collection<Long> patientIds,
            @Param("type") AddressType type
    );

    interface PatientAddressView {
        Long getPatientId();

        String getRegionCode();

        String getDistrictCode();

        String getNeighborhoodCode();

        String getStreetAddress();

        String getHouseNumber();

        String getApartmentNumber();
    }
}
