package uz.uzinfocom.app.platform.iam.application.sync.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.domain.enums.ServiceType;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class OrganizationRemoteMapper {

    public Organization toEntity(RemoteOrganizationPayload payload) {
        UUID uuid = payload.uuid();
        String resolvedName = resolveName(payload);

        return Organization.builder()
                .uuid(uuid)
                .name(resolvedName)
                .nameUz(orDefault(payload.nameUz(), resolvedName))
                .nameUzCyril(orDefault(payload.nameUzCyril(), resolvedName))
                .nameRu(orDefault(payload.nameRu(), resolvedName))
                .nameKaa(orDefault(payload.nameKaa(), resolvedName))
                .active(payload.active() == null || payload.active())
                .levelType(parseLevel(payload.levelCode(), uuid))
                .medicalType(parseMedicalType(payload.medicalTypeCode(), uuid))
                .serviceTypes(parseServiceTypes(payload.serviceTypeCodes(), uuid))
                .regionCode(payload.regionCode())
                .districtCode(payload.districtCode())
                .build();
    }

    /**
     * Guarantees a non-blank organization display name even when the remote
     * system sends neither a name nor any usable alias — falls back to the
     * organization's own uuid so the entity never ends up with a blank name.
     */
    private String resolveName(RemoteOrganizationPayload payload) {
        return StringUtils.hasText(payload.name())
                ? payload.name()
                : payload.uuid().toString();
    }

    /**
     * Every nameXxx() accessor on the payload already falls back to the raw
     * name field when its language-specific alias is missing — but the raw
     * name itself can still be blank/null. This closes that gap by falling
     * back to the already-resolved, guaranteed-non-blank name instead.
     */
    private String orDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private OrganizationLevel parseLevel(String code, UUID organizationUuid) {
        return OrganizationLevel.fromCode(code)
                .orElseGet(() -> {
                    logUnmappedCode("OrganizationLevel", code, organizationUuid);
                    return OrganizationLevel.NOT_DEFINED;
                });
    }

    private MedicalType parseMedicalType(String code, UUID organizationUuid) {
        return MedicalType.fromCode(code)
                .orElseGet(() -> {
                    logUnmappedCode("MedicalType", code, organizationUuid);
                    return MedicalType.OTHER;
                });
    }

    private List<ServiceType> parseServiceTypes(List<String> codes, UUID organizationUuid) {
        return codes.stream()
                .map(code -> mapServiceType(code, organizationUuid))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private ServiceType mapServiceType(String code, UUID organizationUuid) {
        return ServiceType.fromCode(code)
                .orElseGet(() -> {
                    logUnmappedCode("ServiceType", code, organizationUuid);
                    return null;
                });
    }

    private void logUnmappedCode(String enumName, String code, UUID organizationUuid) {
        log.warn(
                "Unmapped {} code '{}' received for organization {}; check for a new FHIR code that needs to be added",
                enumName,
                code,
                organizationUuid
        );
    }
}
