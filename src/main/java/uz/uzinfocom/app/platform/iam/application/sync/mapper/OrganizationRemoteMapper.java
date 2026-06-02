package uz.uzinfocom.app.platform.iam.application.sync.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;

import java.util.Locale;

@Component
public class OrganizationRemoteMapper {

    public Organization toEntity(RemoteOrganizationPayload payload) {
        return Organization.builder()
                .uuid(payload.uuid())
                .name(payload.name() == null || payload.name().isBlank() ? payload.uuid().toString() : payload.name())
                .active(payload.active() == null || payload.active())
                .levelType(parseLevel(payload.levelCode()))
                .medicalType(parseMedicalType(payload.medicalTypeCode()))
                .stateCode(payload.stateCode())
                .cityCode(payload.cityCode())
                .build();
    }

    private OrganizationLevel parseLevel(String value) {
        if (value == null || value.isBlank()) {
            return OrganizationLevel.NOT_DEFINED;
        }
        try {
            return OrganizationLevel.valueOf(normalize(value));
        } catch (IllegalArgumentException ignored) {
            return OrganizationLevel.NOT_DEFINED;
        }
    }

    private MedicalType parseMedicalType(String value) {
        if (value == null || value.isBlank()) {
            return MedicalType.OTHER;
        }
        try {
            return MedicalType.valueOf(normalize(value));
        } catch (IllegalArgumentException ignored) {
            return MedicalType.OTHER;
        }
    }

    private String normalize(String value) {
        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }
}
