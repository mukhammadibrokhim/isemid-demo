package uz.uzinfocom.isemid.features.integrations.gcp.util;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.dtos.ApiException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkNnuzb;

@UtilityClass
public class ValidationUtil {
    public static void validateByNnuzb(String nnuzb) {
        if (!checkNnuzb(nnuzb)) {
            throw new ApiException(BAD_REQUEST, "Неверный NNUZB или неверный параметр запроса фотографии");
        }
    }

    public static void validateChildCertificate(String serial, String number) {
        if (!isValidCertificateSeries(serial)) {
            throw new ApiException(BAD_REQUEST, "Недействительная серия сертификата.");
        }
        if (!isValidNumber(number)) {
            throw new ApiException(BAD_REQUEST, "Недействительный номер сертификата.");
        }
    }

    public static boolean isValidCertificateSeries(String series) {
        String validSeriesPattern = "[A-Z-]+";
        return series != null && series.matches(validSeriesPattern);
    }

    public static boolean isValidNumber(String number) {
        return number != null && number.matches("\\d+");
    }


}
