package uz.uzinfocom.app.platform.reference.application.manualreport.command;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportCreateRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportUpdateRequest;
import uz.uzinfocom.app.platform.reference.domain.ManualReport;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = ReferenceCodeNormalizer.class)
public interface ManualReportCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()))")
    @Mapping(target = "includeInTotal", expression = "java(request.includeInTotal() == null ? Boolean.TRUE : request.includeInTotal())")
    @Mapping(target = "reportTypes", source = "reportTypes", qualifiedByName = "normalizeTags")
    @Mapping(target = "mkb10Codes", source = "mkb10Codes", qualifiedByName = "normalizeMkb10Codes")
    @Mapping(target = "deleted", constant = "false")
    ManualReport toEntity(ManualReportCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()))")
    @Mapping(target = "includeInTotal", expression = "java(request.includeInTotal() == null ? Boolean.TRUE : request.includeInTotal())")
    @Mapping(target = "reportTypes", source = "reportTypes", qualifiedByName = "normalizeTags")
    @Mapping(target = "mkb10Codes", source = "mkb10Codes", qualifiedByName = "normalizeMkb10Codes")
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget ManualReport entity, ManualReportUpdateRequest request);

    @Named("normalizeMkb10Codes")
    default Set<String> normalizeMkb10Codes(Set<String> codes) {
        if (codes == null) {
            return new HashSet<>();
        }

        return codes.stream()
                .filter(StringUtils::hasText)
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Named("normalizeTags")
    default Set<String> normalizeTags(Set<String> tags) {
        if (tags == null) {
            return new HashSet<>();
        }

        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
