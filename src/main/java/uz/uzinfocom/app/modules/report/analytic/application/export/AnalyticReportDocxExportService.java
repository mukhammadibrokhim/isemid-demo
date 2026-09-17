package uz.uzinfocom.app.modules.report.analytic.application.export;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Renders one saved "Analitik hisobot" ({@link AnalyticReportResponse}) as a
 * .docx file — the caller's free-edited {@code content} (plain text, per this
 * module's convention — see {@link AnalyticReportExcelExportSource}) as the
 * document body, under a metadata block reusing the exact same
 * {@code report.analytic_report.export.*}/{@code report.export.shared.*}
 * i18n labels the Excel export already uses, and the same raw (unresolved)
 * region/ICD-10 codes it already shows. Single-record and synchronous — no
 * DB aggregation happens here, so this bypasses {@code ExportJobService}'s
 * background-job/SSE-progress pipeline (built for multi-row exports) and
 * returns the file directly from the controller.
 */
@Component
@RequiredArgsConstructor
public class AnalyticReportDocxExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final MessageResolver messageResolver;

    public byte[] generate(AnalyticReportResponse report) {
        try (XWPFDocument document = new XWPFDocument()) {
            writeTitle(document, report.name());
            writeMetadata(document, report);
            writeContent(document, report.content());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void writeTitle(XWPFDocument document, String name) {
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(name);
        run.setBold(true);
        run.setFontSize(16);
    }

    private void writeMetadata(XWPFDocument document, AnalyticReportResponse report) {
        writeMetadataLine(document, "report.analytic_report.export.status", String.valueOf(report.status()));
        writeMetadataLine(document, "report.export.shared.fromDate", DATE_FORMAT.format(report.fromDate()));
        writeMetadataLine(document, "report.export.shared.toDate", DATE_FORMAT.format(report.toDate()));
        writeMetadataLine(document, "report.analytic_report.export.regionCodes", joinCodes(report.regionCodes()));
        writeMetadataLine(document, "report.analytic_report.export.icd10Codes", joinCodes(report.icd10Codes()));
        writeMetadataLine(document, "report.analytic_report.export.koef", String.valueOf(report.koef()));
        writeMetadataLine(document, "report.export.shared.organizationName", report.organizationName());
        if (report.createdAt() != null) {
            writeMetadataLine(document, "report.export.shared.createdAt",
                    DATE_TIME_FORMAT.format(report.createdAt().atZone(ZoneId.systemDefault())));
        }

        document.createParagraph();
    }

    private void writeMetadataLine(XWPFDocument document, String labelKey, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun labelRun = paragraph.createRun();
        labelRun.setBold(true);
        labelRun.setText(messageResolver.resolve(labelKey) + ": ");

        XWPFRun valueRun = paragraph.createRun();
        valueRun.setText(value);
    }

    private void writeContent(XWPFDocument document, String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        for (String line : content.replace("\r\n", "\n").split("\n", -1)) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(line);
        }
    }

    private String joinCodes(Set<String> codes) {
        return codes == null || codes.isEmpty() ? null : String.join(", ", codes);
    }
}
