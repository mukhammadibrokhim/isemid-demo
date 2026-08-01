package uz.uzinfocom.app.modules.form0581.application.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581QueryService;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581SortFields;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581TableMapper;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelTitleBlock;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tabular Excel export for Form0581, backed by the same {@link Form0581TableMapper}/
 * {@link Form0581TableProjection} the list endpoint already uses. Unlike
 * {@code Form058ExcelExportSource}, this mirrors the on-screen table rather than the
 * official-journal letterhead layout - form0581 has no print-oriented mapper yet (the
 * equivalent of {@code Form058PdfMapper}) to source the extra narrative columns (place of
 * hospitalization, epidemiological investigation, etc.) from. Adding one and switching this
 * source over to it is a self-contained follow-up, not a change to anything below.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Form0581ExcelExportSource implements ExcelExportSource<Form0581Filter, Form0581TableResponse> {

    private final Form0581JpaRepository repository;
    private final Form0581QueryService form0581QueryService;
    private final Form0581TableMapper form0581TableMapper;

    @Override
    public String exportType() {
        return "FORM0581";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form0581Filter filter) {
        return repository.count(form0581QueryService.resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(Form0581Filter filter, Consumer<Form0581TableResponse> rowProcessor) {
        Specification<Form0581> spec = form0581QueryService.resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form0581SortFields.ALLOWED).getSort();

        try (Stream<Form0581TableProjection> projections = repository.findBy(
                spec,
                query -> query.as(Form0581TableProjection.class).sortBy(sort).stream()
        )) {
            projections.forEach(projection -> {
                try {
                    rowProcessor.accept(form0581TableMapper.toTableResponse(projection, filter.direction()));
                } catch (Exception mappingFailure) {
                    // Same rationale as Form058ExcelExportSource: a row referencing a
                    // since-deactivated/merged organization shouldn't abort the whole export.
                    log.warn("event=export_row_skipped exportType=FORM0581 form0581Id={} reason={}",
                            projection.getId(), mappingFailure.getMessage(), mappingFailure);
                }
            });
        }
    }

    @Override
    public List<ExcelColumn<Form0581TableResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", "№", Form0581TableResponse::id),
                ExcelColumn.of("createdAt", "Рўйхатга олинган сана", Form0581TableResponse::createdAt),
                ExcelColumn.of("status", "Ҳолати", row -> row.status() == null ? null : row.status().name()),
                ExcelColumn.of("mkb10", "Ташхис (МКБ-10)", this::diagnosis),
                ExcelColumn.of("senderOrg", "Хабар берувчи муассаса", Form0581TableResponse::senderOrganizationName),
                ExcelColumn.of("receiverOrg", "Хабар қилинган ташкилот", Form0581TableResponse::receiverOrganizationName),
                ExcelColumn.of("patientFio", "Бемор ФИШ", this::patientFio),
                ExcelColumn.of("address", "Доимий манзили", this::patientAddress),
                ExcelColumn.of("source", "Манба", Form0581TableResponse::source)
        );
    }

    @Override
    public List<ExcelTitleBlock> titleBlocks(Form0581Filter filter) {
        int lastCol = availableColumns().size() - 1;
        return List.of(new ExcelTitleBlock("ҲАЙВОН ЧАҚҚАН/ЖАРОҲАТЛАНГАН ШАХСЛАРНИ ҚАЙД ҚИЛИШ ЖУРНАЛИ (форма №058-1)", 0, 0, 0, lastCol));
    }

    @Override
    public String sheetName() {
        return "Форма 058-1";
    }

    @Override
    public String fileNamePrefix() {
        return "form0581_export";
    }

    private String diagnosis(Form0581TableResponse row) {
        if (!StringUtils.hasText(row.mkb10Code()) && !StringUtils.hasText(row.mkb10Name())) {
            return null;
        }
        return StringUtils.hasText(row.mkb10Name()) ? row.mkb10Code() + "-" + row.mkb10Name() : row.mkb10Code();
    }

    private String patientFio(Form0581TableResponse row) {
        if (row.patient() == null) {
            return null;
        }
        return Stream.of(row.patient().lastName(), row.patient().firstName(), row.patient().middleName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    private String patientAddress(Form0581TableResponse row) {
        if (row.patient() == null) {
            return null;
        }
        var patient = row.patient();
        return Stream.of(
                        patient.permanentRegionName(), patient.permanentDistrictName(),
                        patient.permanentNeighborhoodName(), patient.permanentStreetAddress(),
                        patient.permanentHouseNumber()
                )
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }
}
