package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.domain.enums.ServiceType;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.payload.RemoteOrganizationPayload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class OrganizationRemoteMapper {

    public Organization toEntity(RemoteOrganizationPayload payload, String providerKey) {
        UUID uuid = payload.uuid();
        String resolvedName = resolveName(payload);

        return Organization.builder()
                .uuid(uuid)
                .providerKey(providerKey)
                .name(resolvedName)
                .nameUz(payload.nameUz())
                .nameUzCyril(payload.nameUzCyril())
                .nameRu(payload.nameRu())
                .nameKaa(payload.nameKaa())
                .active(payload.active() == null || payload.active())
                .levelType(parseLevel(payload.levelCode(), uuid))
                .medicalType(parseMedicalType(payload.medicalTypeCode(), uuid))
                .serviceTypes(parseServiceTypes(payload.serviceTypeCodes(), uuid))
                .regionCode(payload.regionCode())
                .districtCode(payload.districtCode())
                .build();
    }

    /**
     * Refreshes an already-persisted organization in place from a fresh
     * remote payload — unlike {@link #toEntity}, this never touches
     * {@code uuid} (the lookup identity) or {@code id}/audit fields.
     */
    public void updateEntity(Organization entity, RemoteOrganizationPayload payload, String providerKey) {
        UUID uuid = payload.uuid();

        entity.setProviderKey(providerKey);
        entity.setName(resolveName(payload));
        entity.setNameUz(payload.nameUz());
        entity.setNameUzCyril(payload.nameUzCyril());
        entity.setNameRu(payload.nameRu());
        entity.setNameKaa(payload.nameKaa());
        entity.setActive(payload.active() == null || payload.active());
        entity.setLevelType(parseLevel(payload.levelCode(), uuid));
        entity.setMedicalType(parseMedicalType(payload.medicalTypeCode(), uuid));
        entity.setServiceTypes(parseServiceTypes(payload.serviceTypeCodes(), uuid));
        entity.setRegionCode(payload.regionCode());
        entity.setDistrictCode(payload.districtCode());
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
