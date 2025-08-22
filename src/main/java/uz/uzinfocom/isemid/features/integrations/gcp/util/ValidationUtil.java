package uz.uzinfocom.isemid.features.integrations.gcp.util;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkNnuzb;

@UtilityClass
public class ValidationUtil {
    public static void validateByNnuzb(String nnuzb, MessageUtil messageUtil) {
        if (!checkNnuzb(nnuzb)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.request.invalid_nnuzb", nnuzb));
        }
    }

    public static void validateChildCertificate(String serial, String number, MessageUtil messageUtil) {
        if (!isValidCertificateSeries(serial)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("validation.certificate.series.invalid"));
        }
        if (!isValidNumber(number)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("validation.certificate.number.invalid"));
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
