package com.abdulatif.emis.utility;

import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.model.util.Pagination;
import com.abdulatif.emis.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
public class Utils {
    private static final SecureRandom random = new SecureRandom();
    private static final String NUMERIC = "0123456789";

    public static User currentUser(UserService userService) {
        return userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public static PageRequest pagination(Pagination<?> pagination) {
        return PageRequest.of(pagination.getPage(), pagination.getLimit());
    }

    public static Pageable getPageableFromPagination(Pagination<?> pagination) {
        Pageable paging;
        if (pagination.getSortBy() == null || pagination.getSortBy().isEmpty())
            paging = PageRequest.of(pagination.getPage(), pagination.getLimit(), Sort.by("id").descending());
        else {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();
            List<String> sortBy = pagination.getSortBy();
            List<Boolean> sortDesc = pagination.getSortDesc();
            for (int i = 0; i < sortBy.size(); i++) {
                Sort.Direction direction;
                if (sortDesc.get(i))
                    direction = Sort.Direction.DESC;
                else direction = Sort.Direction.ASC;
                Sort.Order order = new Sort.Order(direction, sortBy.get(i));
                orders.add(order);
            }
            paging = PageRequest.of(pagination.getPage(), pagination.getLimit(), Sort.by(orders));
        }

        return paging;
    }


    public static String generateBarCode() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        String m = month < 10 ? "0" + month : String.valueOf(month);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String d = day < 10 ? "0" + day : String.valueOf(day);
        return (calendar.get(Calendar.YEAR) +
                m +
                d +
                generateBarCode(4)).substring(2);
    }

    public static String generateUnique() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String generateBarCode(int len) {
        return generatePassword(len, NUMERIC);
    }

    public static String generatePassword(int len, String dic) {
        String result = "";
        for (int i = 0; i < len; i++) {
            int index = random.nextInt(dic.length());
            result += dic.charAt(index);
        }
        return result;
    }

    public static List<Map<Object, Object>> parseExcelFile(MultipartFile file) throws Throwable {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        List<Object> header = new ArrayList<>();
        List<Map<Object, Object>> finish = new ArrayList<>();
        long counter = -1L;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            counter++;
            Map<Object, Object> getExcel = null;
            int length = 0;
            if (counter > 0) {
                length = header.size();
                getExcel = new LinkedHashMap<>();
            }
            int sizeCounter = 0;

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                CellType cellType = cell.getCellType();
                switch (cellType) {
                    case _NONE:
                        if (counter == 0)
                            header.add(cell.getStringCellValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), "");
                        break;
                    case BOOLEAN:
                        if (counter == 0)
                            header.add(cell.getBooleanCellValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), cell.getBooleanCellValue());
                        break;
                    case BLANK:
                        if (counter == 0)
                            header.add(" ");
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), " ");
                        break;
                    case FORMULA:
                        if (counter == 0)
                            header.add(cell.getCellFormula());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), cell.getCellFormula());
                        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                        if (counter == 0)
                            header.add(evaluator.evaluate(cell).getNumberValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), cell.getCellFormula());
                        break;
                    case NUMERIC:
                        if (counter == 0)
                            header.add(cell.getNumericCellValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), cell.getNumericCellValue());
                        break;
                    case STRING:
                        if (counter == 0)
                            header.add(cell.getStringCellValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), cell.getStringCellValue());
                        break;
                    case ERROR:
                        if (counter == 0)
                            header.add(cell.getStringCellValue());
                        if (counter > 0)
                            getExcel.put(header.get(sizeCounter), "!");
                        break;
                }
                if (length > sizeCounter) {
                    sizeCounter++;
                }
            }
            if (counter > 0) {
                finish.add(getExcel);
            }
        }
        return finish;
    }

    public static String getConstantMessage(String message, String language) {
        String baseName = "constantMessage";
        return switch (language) {
            case "UZ":
                yield ResourceBundle.getBundle(baseName, new Locale(language, "uz")).getString(message);
            case "RU":
                yield ResourceBundle.getBundle(baseName, new Locale(language, "ru")).getString(message);
            case "EN":
                yield ResourceBundle.getBundle(baseName, new Locale(language, "en")).getString(message);
            default:
                yield ("Not Fount Language: " + language);
        };
    }

    public static String removeLeadingZeros(String str) {
        String regex = "^0+(?!$)";
        str = str.replaceAll(regex, "");
        return str;
    }
}
