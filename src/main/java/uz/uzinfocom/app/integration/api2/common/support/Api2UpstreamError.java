package uz.uzinfocom.app.integration.api2.common.support;

public record Api2UpstreamError(
        Integer status,
        String code,
        String message,
        String detail
) {
}
