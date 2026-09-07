package uz.uzinfocom.app.modules.report.form4.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form4.application.query.Form4ReportQueryService;
import uz.uzinfocom.app.modules.report.form4.application.query.dto.Form4ReportNodeResponse;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyExportFlattener;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;

import java.util.List;
import java.util.function.Consumer;

/**
 * Excel export for "Form 4" (geography-first, social/occupation category
 * breakdown) — mirrors {@code Form1ExcelExportSource} exactly, just over
 * {@link Form4ReportNodeResponse}'s 12-category count blocks.
 */
@Component
@RequiredArgsConstructor
public class Form4ExcelExportSource implements ExcelExportSource<Form4ExportFilter, Form4ReportNodeResponse> {

    private final Form4ReportQueryService form4ReportQueryService;
    private final ReportHierarchyExportFlattener flattener;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM4";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form4ExportFilter filter) {
        return flattenAll(filter).size();
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form4ExportFilter filter, Consumer<Form4ReportNodeResponse> rowProcessor) {
        flattenAll(filter).forEach(rowProcessor);
    }

    private List<Form4ReportNodeResponse> flattenAll(Form4ExportFilter filter) {
        List<Form4ReportNodeResponse> root = form4ReportQueryService.getRoot(
                filter.from(), filter.to(), filter.diagnosisCode()
        );
        return flattener.flattenAll(
                root,
                (regionCode, districtCode) -> form4ReportQueryService.getChildren(
                        regionCode, districtCode, filter.from(), filter.to(), filter.diagnosisCode()
                ),
                Form4ReportNodeResponse::code,
                Form4ReportNodeResponse::hasChildren
        );
    }

    @Override
    public List<ExcelColumn<Form4ReportNodeResponse>> availableColumns() {
        String confirmed = messageResolver.resolve("report.export.block.confirmed");
        String primary = messageResolver.resolve("report.export.block.primary");
        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form4ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form4ReportNodeResponse::name),
                ExcelColumn.of("confirmedTotal", cat(confirmed, "report.export.block.total"), row -> row.confirmed().total()),
                ExcelColumn.of("confirmedUnorganizedPreschool", cat(confirmed, "report.form4.export.cat.unorganizedPreschool"), row -> row.confirmed().unorganizedPreschool()),
                ExcelColumn.of("confirmedOrganizedPreschool", cat(confirmed, "report.form4.export.cat.organizedPreschool"), row -> row.confirmed().organizedPreschool()),
                ExcelColumn.of("confirmedSchoolStudents", cat(confirmed, "report.form4.export.cat.schoolStudents"), row -> row.confirmed().schoolStudents()),
                ExcelColumn.of("confirmedVocationalStudents", cat(confirmed, "report.form4.export.cat.vocationalStudents"), row -> row.confirmed().vocationalStudents()),
                ExcelColumn.of("confirmedUniversityStudents", cat(confirmed, "report.form4.export.cat.universityStudents"), row -> row.confirmed().universityStudents()),
                ExcelColumn.of("confirmedEmployees", cat(confirmed, "report.form4.export.cat.employees"), row -> row.confirmed().employees()),
                ExcelColumn.of("confirmedWorkers", cat(confirmed, "report.form4.export.cat.workers"), row -> row.confirmed().workers()),
                ExcelColumn.of("confirmedMedicalStaff", cat(confirmed, "report.form4.export.cat.medicalStaff"), row -> row.confirmed().medicalStaff()),
                ExcelColumn.of("confirmedUnemployed", cat(confirmed, "report.form4.export.cat.unemployed"), row -> row.confirmed().unemployed()),
                ExcelColumn.of("confirmedPensioners", cat(confirmed, "report.form4.export.cat.pensioners"), row -> row.confirmed().pensioners()),
                ExcelColumn.of("confirmedUnsheltered", cat(confirmed, "report.form4.export.cat.unsheltered"), row -> row.confirmed().unsheltered()),
                ExcelColumn.of("primaryTotal", cat(primary, "report.export.block.total"), row -> row.primary().total()),
                ExcelColumn.of("primaryUnorganizedPreschool", cat(primary, "report.form4.export.cat.unorganizedPreschool"), row -> row.primary().unorganizedPreschool()),
                ExcelColumn.of("primaryOrganizedPreschool", cat(primary, "report.form4.export.cat.organizedPreschool"), row -> row.primary().organizedPreschool()),
                ExcelColumn.of("primarySchoolStudents", cat(primary, "report.form4.export.cat.schoolStudents"), row -> row.primary().schoolStudents()),
                ExcelColumn.of("primaryVocationalStudents", cat(primary, "report.form4.export.cat.vocationalStudents"), row -> row.primary().vocationalStudents()),
                ExcelColumn.of("primaryUniversityStudents", cat(primary, "report.form4.export.cat.universityStudents"), row -> row.primary().universityStudents()),
                ExcelColumn.of("primaryEmployees", cat(primary, "report.form4.export.cat.employees"), row -> row.primary().employees()),
                ExcelColumn.of("primaryWorkers", cat(primary, "report.form4.export.cat.workers"), row -> row.primary().workers()),
                ExcelColumn.of("primaryMedicalStaff", cat(primary, "report.form4.export.cat.medicalStaff"), row -> row.primary().medicalStaff()),
                ExcelColumn.of("primaryUnemployed", cat(primary, "report.form4.export.cat.unemployed"), row -> row.primary().unemployed()),
                ExcelColumn.of("primaryPensioners", cat(primary, "report.form4.export.cat.pensioners"), row -> row.primary().pensioners()),
                ExcelColumn.of("primaryUnsheltered", cat(primary, "report.form4.export.cat.unsheltered"), row -> row.primary().unsheltered())
        );
    }

    private String cat(String prefix, String suffixKey) {
        return prefix + " — " + messageResolver.resolve(suffixKey);
    }

    @Override
    public String sheetName() {
        return "Shakl 4";
    }

    @Override
    public String fileNamePrefix() {
        return "form4_export";
    }
}
