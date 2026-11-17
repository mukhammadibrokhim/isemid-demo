package uz.uzinfocom.app.modules.reference.application.geosync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Envelope of {@code GET /apiPartner/Table/Get}: {@code {"result":{"count":N,"data":[...]},"statusCode":200,"error":null}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EgovTableResponse<T>(Result<T> result, Integer statusCode, String error) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result<T>(Integer count, List<T> data) {

        public List<T> rows() {
            return data == null ? List.of() : data;
        }
    }
}
