package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.patient.models.Address;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.report.service.LocationReportService;
import uz.uzinfocom.isemid.features.report.utils.LocationReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationReportServiceImpl implements LocationReportService {
    private final Form058Repository form058Repository;
    private final LocationReportHelper helper;

    @Override
    public LocationReportDto getByLocation(
            String mkb10Code,
            LocalDate fromDate,
            LocalDate toDate,
            LevelType level,
            String parentCode,
            User currentUser
    ) {
        Specification<Form058> spec = this.build(mkb10Code, fromDate, toDate, currentUser);
        List<Form058> forms = form058Repository.findAll(spec);

        if (level == null) {
            level = currentUser.getCurrentOrganization().getLevelType();
        }

        if (parentCode != null && !parentCode.isBlank()) {
            return helper.buildByParentCode(parentCode, forms);
        }

        return switch (level) {
            case REPUBLICAN -> helper.buildRegionReports(forms);
            case REGIONAL, URBAN -> helper.buildDistrictReports(parentCode, forms);
            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    helper.buildNeighborhoodReports(parentCode, forms);
        };
    }

    private Specification<Form058> build(
            final String mkb10Code,
            final LocalDate fromDate,
            final LocalDate toDate,
            User currentUser
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("mkb10Code"), mkb10Code));
            }
            fromToFilter(fromDate, toDate, root, cb, predicates);

            Join<Form058, Patient> patient = root.join("patient");
            Join<Patient, Address> addresses = patient.join("addresses");
            predicates.add(cb.equal(addresses.get("type"), AddressType.TEMPORARY));

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                var org = currentUser.getCurrentOrganization();
                var level = org.getLevelType();

                switch (level) {
                    case REPUBLICAN -> {}
                    case REGIONAL, URBAN -> predicates.add(cb.equal(addresses.get("stateCode"), org.getStateCode()));
                    case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> predicates.add(cb.equal(addresses.get("cityCode"), org.getCityCode()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static void fromToFilter(LocalDate fromDate, LocalDate toDate, Root<Form058> root, CriteriaBuilder cb,
                                    List<Predicate> predicates) {
        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(
                    root.get("createdAt"),
                    Timestamp.valueOf(fromDate.atStartOfDay()),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        } else if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    Timestamp.valueOf(fromDate.atStartOfDay())
            ));
        } else if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        }
    }

}
