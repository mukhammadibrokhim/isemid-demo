package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.enums.FilterType;
import uz.uzinfocom.isemid.models.payload.requests.form.*;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.Form058Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.FORM0058;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(FORM0058)
@Tag(name = "Form 058", description = "API для работы с формой 058")
@RequiredArgsConstructor
public class Form058Controller {

    final Form058Service form058Service;

    @Operation(
            summary = "Создание формы 058",
            description = "Создаёт новую форму 058 с данными пациента и другой необходимой информацией.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form058> create(
            @Valid @RequestBody ReqForm058 reqForm058,
            @CurrentUser User user) {
        return form058Service.createForm058(reqForm058, user);
    }

    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getFilteredForm058(page, size, user, getFullURL(request), FilterType.ALL);
    }

    @GetMapping("/all/export")
    public ResponseEntity<byte[]> exportFullJournal060() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Журнал 060");
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

        // Styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle rotatedStyle = workbook.createCellStyle();
        rotatedStyle.cloneStyleFrom(headerStyle);
        rotatedStyle.setRotation((short) 90); // vertical text

        int rowIdx = 0;

        // Header part
        rowIdx = merge(sheet, rowIdx, "Ўзбекистон Республикаси", headerStyle, 16);
        rowIdx = merge(sheet, rowIdx, "Соғлиқни сақлаш Вазирлиги", headerStyle, 16);
        rowIdx = merge(sheet, rowIdx, "_________________________", null, 16);
        rowIdx = merge(sheet, rowIdx, "Муассаса номи", null, 16);

        rowIdx = merge(sheet, rowIdx, "Ўзбекистон Республикаси Соғлиқни сақлаш вазирининг 2020 йил 31 декабрдаги 363 -сонли буйруғи билан тасдиқланган", null, 16);
        rowIdx = merge(sheet, rowIdx, "060- рақамли тиббий ҳужжат шакли", headerStyle, 16);
        rowIdx = merge(sheet, rowIdx, "ЮҚУМЛИ КАСАЛЛИКЛАРНИ ҚАЙД ҚИЛИШ ЖУРНАЛИ", headerStyle, 16);
        rowIdx = merge(sheet, rowIdx, "Бошланган сана   «_____» ___________________________20____ йил     Эслатма: 13 – 14 графалар фақат санитария -эпидемиология назорат марказларида тўлдирилади.", null, 16);
        rowIdx = merge(sheet, rowIdx, "Тугалланган сана  «_______» _________________________20____ йил", null, 16);

        // Table header rows (3 rows total)
        Row r1 = sheet.createRow(rowIdx++);
        Row r2 = sheet.createRow(rowIdx++);
        Row r3 = sheet.createRow(rowIdx++);

        String[] topHeaders = {
                "№", "Бирламчи тезкор хабар...", "Хабар берувчи даволаш...", "Беморнинг ФИШ", "Ёши", "Уй манзили", "Иш жойи, ўқиш...", "Касал бўлган куни",
                "Биринчи марта мурожаат", "Ётқизилган жой", "Ташхис", "Ўзгарган ташхис", "Эпидемиологик текширув", "Касаллик ҳақида хабар қилинган жой", "Лаборатория натижаси", "Эслатма"
        };

        // Merge vertically for 1st row items (№, ФИШ, Ёши...)
        for (int i = 0; i < topHeaders.length; i++) {
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 3, rowIdx - 1, i, i));
            Cell cell = r1.createCell(i);
            cell.setCellValue(topHeaders[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // Example for vertical text on columns
        r1.getCell(0).getCellStyle().setRotation((short) 90);

        // Add 5 rows of dummy data
        for (int i = 0; i < 5; i++) {
            Row row = sheet.createRow(rowIdx++);
            for (int j = 0; j < topHeaders.length; j++) {
                Cell cell = row.createCell(j);
                if (j == 0) cell.setCellValue(i + 1);
                else cell.setCellValue("");
                cell.setCellStyle(headerStyle);
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("journal060_full.xlsx").build());

        return new ResponseEntity<>(bos.toByteArray(), headers, HttpStatus.OK);
    }

    private int merge(Sheet sheet, int rowIdx, String text, CellStyle style, int colspan) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        if (style != null) cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, colspan - 1));
        return rowIdx + 1;
    }

    @GetMapping("/received/all")
    public ApiResponsePage getReceivedAll(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getFilteredForm058(page, size, user, getFullURL(request), FilterType.RECEIVED);
    }

    @GetMapping("/received/all/export")
    public String exportReceivedAll() {
        return "";
    }

    @GetMapping("/{id}")
    public ApiResponse<Form058> get(@PathVariable long id) {
        return form058Service.getById(id);
    }

    @Operation(
            summary = "Обновление формы 058",
            description = "Обновляет существующую форму 058 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form058> update(
            @RequestBody ReqForm058 reqForm058,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return form058Service.updateForm058(reqForm058, id, user);
    }

    @Operation(summary = "Назначение карты для Form058", description = "Этот эндпоинт назначает карту (Card) для существующей записи Form058. ")
    @PostMapping("/assign-card")
    public ApiResponse<Form058> assignCard(@Valid @RequestBody ReqForm058AssignCard assignCard,@CurrentUser User user) {
        return form058Service.assignCard(assignCard,user);
    }

    @Operation(summary = "Перевод Form058 в другую организацию",
            description = "Этот эндпоинт позволяет перевести форму Form058 в другую организацию. ")
    @PutMapping("/{formId}/change-received-org")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId,
            @Valid @RequestBody ReqForm058OrgUpdate reqForm058OrgUpdate
            ) {
        return form058Service.changeReceivedOrd(formId, reqForm058OrgUpdate);
    }

    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long formId) {
        return form058Service.cancel(formId);
    }

    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId) {
        return form058Service.approveForm(formId);
    }

    @PutMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(@PathVariable Long formId, @RequestBody ReqForm058NotApprove notApprove) {
        return form058Service.notApprove(formId, notApprove);
    }

    @PutMapping("/{formId}/change-diagnosis")
    public ApiResponse<?> changeDiagnosis(@PathVariable Long formId, ReqForm058ChangeDiagnosis changeDiagnosis) {
        return form058Service.changeDiagnosis(formId, changeDiagnosis);
    }

    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        return form058Service.deleteById(formId);
    }
}

