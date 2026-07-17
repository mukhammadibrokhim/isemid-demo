package uz.uzinfocom.isemid.commons.exceptions.handler;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;

@FunctionalInterface
public interface ErrorDecoder {
    ApiException decode(HttpStatus status, String responseBody);
}
