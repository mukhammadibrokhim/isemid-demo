package uz.uzinfocom.app.platform.reference.application.mkb10.command;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10CreateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10UpdateRequest;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;

@Mapper(componentModel = "spring", uses = Mkb10ParentResolver.class, imports = ReferenceCodeNormalizer.class)
public interface Mkb10CommandMapper {

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()).toUpperCase(java.util.Locale.ROOT))")
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "resolveMkb10Parent")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "usageLimit", expression = "java(request.usageLimit() == null ? 1 : request.usageLimit())")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "children", ignore = true)
    Mkb10 toEntity(Mkb10CreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", expression = "java(ReferenceCodeNormalizer.normalizeCode(request.code()).toUpperCase(java.util.Locale.ROOT))")
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "resolveMkb10Parent")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "usageLimit", expression = "java(request.usageLimit() == null ? 1 : request.usageLimit())")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateEntity(@MappingTarget Mkb10 entity, Mkb10UpdateRequest request);
}
