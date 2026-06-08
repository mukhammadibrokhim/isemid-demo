package uz.uzinfocom.isemid.features.report.service;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto.ReportDto;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRow;
import uz.uzinfocom.isemid.features.report.repository.LocationReportRepository;
import uz.uzinfocom.isemid.features.report.service.scope.ReportScope;
import uz.uzinfocom.isemid.features.report.service.scope.ReportScopeResolver;
import uz.uzinfocom.isemid.features.user.models.User;

/**
 * Location disease-cases report. Orchestration only: resolve the report scope from the
 * requesting organization, issue one set-based query, and map the projected rows into the
 * response DTO. All filtering, hierarchy resolution, visibility rules, and aggregation are
 * performed in PostgreSQL.
 */
@Slf4j
@Service("nationalDiseaseCasesReport")
public class NationalDiseaseCasesReport implements LocationReportService {

    private final LocationReportRepository repository;
    private final ReportScopeResolver scopeResolver;

    public NationalDiseaseCasesReport(LocationReportRepository repository,
                                      ReportScopeResolver scopeResolver) {
        this.repository = repository;
        this.scopeResolver = scopeResolver;
    }

    @Override
    public LocationReportDto getByLocation(String mkb10Code, LocalDate fromDate, LocalDate toDate,
                                           String parentCode, User currentUser) {

        Organization org = currentUser.getCurrentOrganization();
        ReportScope scope = scopeResolver.resolve(org, parentCode);
        String locale = LocaleContextHolder.getLocale().getLanguage();

        // toDate is an exclusive upper bound: the query filters created_at < :toDate,
        // so a record stamped exactly on toDate is not counted (half-open interval).
        List<LocationReportRow> rows =
                repository.fetch(mkb10Code, fromDate, toDate, scope, locale);



        List<ReportDto> reports = rows.stream()
                .filter(row -> !row.total())
                .map(row -> ReportDto.builder()
                        .code(row.code())
                        .name(row.name())
                        .confirmed(row.confirmed())
                        .unconfirmed(row.unconfirmed())
                        .build())
                .toList();

        LocationReportRow total = rows.stream()
                .filter(LocationReportRow::total)
                .findFirst()
                .orElse(new LocationReportRow(null, null, 0, 0, true));

        return LocationReportDto.builder()
                .reports(reports)
                .totalConfirmed(total.confirmed())
                .totalUnconfirmed(total.unconfirmed())
                .totalDocuments(total.confirmed() + total.unconfirmed())
                .level(scope.dimension().level())
                .build();
    }
}
