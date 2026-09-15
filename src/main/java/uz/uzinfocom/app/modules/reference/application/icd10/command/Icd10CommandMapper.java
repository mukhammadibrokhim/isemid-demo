package uz.uzinfocom.app.modules.reference.application.icd10.command;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10CreateRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10UpdateRequest;
import uz.uzinfocom.app.modules.reference.domain.Icd10;

@Mapper(componentModel = "spring", uses = Icd10ParentResolver.class, imports = ReferenceCodeNormalizer.class)
public interface Icd10CommandMapper {

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()).toUpperCase(java.util.Locale.ROOT))")
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "resolveIcd10Parent")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "usageLimit", expression = "java(request.usageLimit() == null ? 1 : request.usageLimit())")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "children", ignore = true)
    Icd10 toEntity(Icd10CreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()).toUpperCase(java.util.Locale.ROOT))")
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "resolveIcd10Parent")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "usageLimit", expression = "java(request.usageLimit() == null ? 1 : request.usageLimit())")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateEntity(@MappingTarget Icd10 entity, Icd10UpdateRequest request);
}
