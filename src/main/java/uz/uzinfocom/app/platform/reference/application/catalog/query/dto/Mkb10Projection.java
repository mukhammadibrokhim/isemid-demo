package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

public record Mkb10Projection(
        String code,
        String name
) {

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
