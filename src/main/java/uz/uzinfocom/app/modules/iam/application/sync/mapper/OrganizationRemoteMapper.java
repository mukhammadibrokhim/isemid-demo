package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.domain.enums.ServiceType;
import uz.uzinfocom.app.modules.iam.infrastructure.remote.payload.RemoteOrganizationPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationRemoteMapper {

    public Organization toEntity(RemoteOrganizationPayload payload, String providerKey) {
        UUID uuid = payload.uuid();
        LocalizedNames names = resolveNames(payload);

        return Organization.builder()
                .uuid(uuid)
                .providerKey(providerKey)
                .nameUz(names.uz())
                .nameUzCyril(names.uzCyril())
                .nameRu(names.ru())
                .nameKaa(names.kaa())
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
        LocalizedNames names = resolveNames(payload);
        entity.setNameUz(names.uz());
        entity.setNameUzCyril(names.uzCyril());
        entity.setNameRu(names.ru());
        entity.setNameKaa(names.kaa());
        entity.setActive(payload.active() == null || payload.active());
        entity.setLevelType(parseLevel(payload.levelCode(), uuid));
        entity.setMedicalType(parseMedicalType(payload.medicalTypeCode(), uuid));
        entity.setServiceTypes(parseServiceTypes(payload.serviceTypeCodes(), uuid));
        entity.setRegionCode(payload.regionCode());
        entity.setDistrictCode(payload.districtCode());
    }

    private record LocalizedNames(String uz, String uzCyril, String ru, String kaa) {
    }

    /**
     * Per-language names from the remote aliases. The remote's own base
     * {@code name} carries no language tag, so it is slotted into the column
     * matching its script — but only where no alias already filled it. If the
     * remote sends nothing at all, the organization's uuid keeps the name
     * from being blank.
     */
    private LocalizedNames resolveNames(RemoteOrganizationPayload payload) {
        String uz = payload.nameUz();
        String uzCyril = payload.nameUzCyril();
        String ru = payload.nameRu();
        String kaa = payload.nameKaa();

        String base = payload.name();
        if (StringUtils.hasText(base)) {
            base = base.trim();
            if (!containsCyrillic(base)) {
                uz = StringUtils.hasText(uz) ? uz : base;
            } else if (containsUzbekCyrillicLetter(base)) {
                uzCyril = StringUtils.hasText(uzCyril) ? uzCyril : base;
            } else {
                ru = StringUtils.hasText(ru) ? ru : base;
            }
        }

        if (!StringUtils.hasText(uz) && !StringUtils.hasText(uzCyril)
                && !StringUtils.hasText(ru) && !StringUtils.hasText(kaa)) {
            uz = payload.uuid().toString();
        }

        return new LocalizedNames(uz, uzCyril, ru, kaa);
    }

    private static boolean containsCyrillic(String value) {
        return value.codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.CYRILLIC);
    }

    private static boolean containsUzbekCyrillicLetter(String value) {
        return value.chars().anyMatch(ch -> "ЎўҚқҒғҲҳ".indexOf(ch) >= 0);
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
        // Mutable on purpose: Hibernate merge()/flush clears and refills an
        // element-collection in place, which throws on an immutable List.
        return codes.stream()
                .map(code -> mapServiceType(code, organizationUuid))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
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
