package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.utils.CatalogLocaleUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form1ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form1ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form058StatisticsUtil;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form1ReportServiceImpl implements Form1ReportService {
    private final Form058Repository form058Repository;
    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    @Override
    public Object getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, String mkb10Code, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, mkb10Code,gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);
        Organization organization = currentUser.getCurrentOrganization();
        String level = organization.getLevelType().name();

        if (level.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, messageUtil.get("error.organization.level.code.missing"));
        }

        if (level.equals(LevelType.REPUBLICAN.name())) {
            return createRepublicReport(form058s);

        } else if (level.equals(LevelType.REGIONAL.name())) {
            return creteRegionReport(form058s, organization);

        } else if (level.equals(LevelType.DISTRICT.name()) || level.equals(LevelType.AREA.name()) || level.equals(LevelType.URBAN.name())
                || level.equals(LevelType.INTERDISTRICT.name()) || level.equals(LevelType.NOT_DEFINED.name())) {
            return creteDistrictReport(form058s, organization);

        }
        throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, messageUtil.get("error.organization.level.invalid"));
    }

    private Object creteRegionReport(List<Form058> form058s, Organization organization) {
        String regionCode = organization.getState().getCode();
        String regionName = CatalogLocaleUtil.displayName(organization.getState());

        List<Form058> regionForms = form058s.stream()
                .filter(f -> f.getCreatedOrg() != null &&
                        f.getCreatedOrg().getState() != null &&
                        regionCode.equals(f.getCreatedOrg().getState().getCode()))
                .toList();

        Set<Form1ReportDto> children = Form058StatisticsUtil.groupAndCalculate(
                regionForms,
                f -> f.getCreatedOrg().getId().toString(), // guruhlash: tashkilot ID bo‘yicha
                f -> f.getCreatedOrg().getName()           // nom: tashkilot nomi
        );

        Form1ReportDto regionReport = Form058StatisticsUtil.calculate(regionForms, regionName, children);

        return List.of(regionReport);
    }


    private Object creteDistrictReport(List<Form058> form058s, Organization organization) {
        List<Form058> districtForms = form058s.stream()
                .filter(f -> f.getCreatedOrg().getCity().getCode().equals(organization.getCity().getCode()))
                .toList();

        String districtName = CatalogLocaleUtil.displayName(organization.getCity());

        Set<Form1ReportDto> organizationReports = Form058StatisticsUtil.groupAndCalculate(
                districtForms,
                f -> f.getCreatedOrg().getId().toString(),
                f -> f.getCreatedOrg().getName()
        );

        Form1ReportDto districtReport = Form058StatisticsUtil.calculate(districtForms, districtName, organizationReports);

        return List.of(districtReport);
    }


    private Object createRepublicReport(List<Form058> form058s) {
        List<Catalog> states = catalogService.finAllByType(CatalogType.state);

        List<Form1ReportDto> reportList = new LinkedList<>();

        Set<Form1ReportDto> childrenStates = states.stream().map(state -> {
            List<Form058> stateForms = form058s.stream()
                    .filter(f -> f.getCreatedOrg() != null
                            && f.getCreatedOrg().getState() != null
                            && f.getCreatedOrg().getState().getCode().equals(state.getCode()))
                    .toList();

            Set<Form1ReportDto> districtChildren = new HashSet<>();

            if (!stateForms.isEmpty()) {
                String regionCode = stateForms.getFirst().getCreatedOrg().getStateCode();

                if (regionCode != null && !regionCode.isEmpty()) {
                    Map<String, List<Form058>> cityMap = stateForms.stream()
                            .filter(f -> f.getCreatedOrg().getCity().getParentCode().equals(regionCode))
                            .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCityCode()));

                    for (Map.Entry<String, List<Form058>> cityEntry : cityMap.entrySet()) {
                        List<Form058> cityForms = cityEntry.getValue();
                        String districtName = cityForms.getFirst().getCreatedOrg().getName();

                        districtChildren.add(Form058StatisticsUtil.calculate(cityForms, districtName));
                    }
                }
            }

            return Form058StatisticsUtil.calculate(stateForms, CatalogLocaleUtil.displayName(state), districtChildren);
        }).collect(Collectors.toSet());

        reportList.add(Form058StatisticsUtil.calculate(
                form058s, messageUtil.get("country.name"), childrenStates));

        return reportList;
    }

    public static Specification<Form058> filterByCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Organization userOrg = currentUser.getCurrentOrganization();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (birthDate != null) {
                predicates.add(cb.equal(root.get("patient").get("birthDate"), birthDate));
            }

            if (gender != null) {
                predicates.add(cb.equal(root.get("patient").get("genderCode"), gender.name()));
            }

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("mkb10Code"), mkb10Code));
            }
            predicates.add(cb.notEqual(root.get("status"), FormStatus.CANCELED));

            if (userOrg != null && userOrg.getMedicalType() == MedicalType.SANEPID_SERVICE) {
                SpecificationUtils.addSanepidAccessPredicate(root, cb, userOrg, predicates);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
