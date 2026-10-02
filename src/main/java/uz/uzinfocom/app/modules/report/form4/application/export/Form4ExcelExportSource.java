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
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
        String confirmedGroup = messageResolver.resolve("report.form4.export.group.confirmed");
        String primaryGroup = messageResolver.resolve("report.form4.export.group.primary");
        String total = messageResolver.resolve("report.export.block.total");
        String unorganizedPreschool = messageResolver.resolve("report.form4.export.cat.unorganizedPreschool");
        String organizedPreschool = messageResolver.resolve("report.form4.export.cat.organizedPreschool");
        String schoolStudents = messageResolver.resolve("report.form4.export.cat.schoolStudents");
        String vocationalStudents = messageResolver.resolve("report.form4.export.cat.vocationalStudents");
        String universityStudents = messageResolver.resolve("report.form4.export.cat.universityStudents");
        String employees = messageResolver.resolve("report.form4.export.cat.employees");
        String workers = messageResolver.resolve("report.form4.export.cat.workers");
        String medicalStaff = messageResolver.resolve("report.form4.export.cat.medicalStaff");
        String unemployed = messageResolver.resolve("report.form4.export.cat.unemployed");
        String pensioners = messageResolver.resolve("report.form4.export.cat.pensioners");
        String unsheltered = messageResolver.resolve("report.form4.export.cat.unsheltered");

        return List.of(
                ExcelColumn.of("code", messageResolver.resolve("report.export.geo.code"), Form4ReportNodeResponse::code),
                ExcelColumn.of("name", messageResolver.resolve("report.export.geo.territory"), Form4ReportNodeResponse::name),
                ExcelColumn.of("confirmedTotal", confirmedGroup, total, row -> row.confirmed().total()),
                ExcelColumn.of("confirmedUnorganizedPreschool", confirmedGroup, unorganizedPreschool, row -> row.confirmed().unorganizedPreschool()),
                ExcelColumn.of("confirmedOrganizedPreschool", confirmedGroup, organizedPreschool, row -> row.confirmed().organizedPreschool()),
                ExcelColumn.of("confirmedSchoolStudents", confirmedGroup, schoolStudents, row -> row.confirmed().schoolStudents()),
                ExcelColumn.of("confirmedVocationalStudents", confirmedGroup, vocationalStudents, row -> row.confirmed().vocationalStudents()),
                ExcelColumn.of("confirmedUniversityStudents", confirmedGroup, universityStudents, row -> row.confirmed().universityStudents()),
                ExcelColumn.of("confirmedEmployees", confirmedGroup, employees, row -> row.confirmed().employees()),
                ExcelColumn.of("confirmedWorkers", confirmedGroup, workers, row -> row.confirmed().workers()),
                ExcelColumn.of("confirmedMedicalStaff", confirmedGroup, medicalStaff, row -> row.confirmed().medicalStaff()),
                ExcelColumn.of("confirmedUnemployed", confirmedGroup, unemployed, row -> row.confirmed().unemployed()),
                ExcelColumn.of("confirmedPensioners", confirmedGroup, pensioners, row -> row.confirmed().pensioners()),
                ExcelColumn.of("confirmedUnsheltered", confirmedGroup, unsheltered, row -> row.confirmed().unsheltered()),
                ExcelColumn.of("primaryTotal", primaryGroup, total, row -> row.primary().total()),
                ExcelColumn.of("primaryUnorganizedPreschool", primaryGroup, unorganizedPreschool, row -> row.primary().unorganizedPreschool()),
                ExcelColumn.of("primaryOrganizedPreschool", primaryGroup, organizedPreschool, row -> row.primary().organizedPreschool()),
                ExcelColumn.of("primarySchoolStudents", primaryGroup, schoolStudents, row -> row.primary().schoolStudents()),
                ExcelColumn.of("primaryVocationalStudents", primaryGroup, vocationalStudents, row -> row.primary().vocationalStudents()),
                ExcelColumn.of("primaryUniversityStudents", primaryGroup, universityStudents, row -> row.primary().universityStudents()),
                ExcelColumn.of("primaryEmployees", primaryGroup, employees, row -> row.primary().employees()),
                ExcelColumn.of("primaryWorkers", primaryGroup, workers, row -> row.primary().workers()),
                ExcelColumn.of("primaryMedicalStaff", primaryGroup, medicalStaff, row -> row.primary().medicalStaff()),
                ExcelColumn.of("primaryUnemployed", primaryGroup, unemployed, row -> row.primary().unemployed()),
                ExcelColumn.of("primaryPensioners", primaryGroup, pensioners, row -> row.primary().pensioners()),
                ExcelColumn.of("primaryUnsheltered", primaryGroup, unsheltered, row -> row.primary().unsheltered())
        );
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form4ExportFilter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(
                new ExcelTitleBlock(messageResolver.resolve("report.form4.export.title"), 0, 0, 0, lastCol),
                new ExcelTitleBlock(periodText(filter), 1, 1, 0, lastCol)
        );
    }

    private String periodText(Form4ExportFilter filter) {
        String from = filter.from() == null ? "—" : DATE_FORMAT.format(filter.from());
        String to = filter.to() == null ? "—" : DATE_FORMAT.format(filter.to());
        return messageResolver.resolve("report.form4.export.period", from, to);
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
