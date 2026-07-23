package uz.uzinfocom.app.modules.act.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConditionInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConservationTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.EmployeeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Institution;
import uz.uzinfocom.app.modules.act.domain.model.embedded.PackageTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.SampleTypeInfo;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConservationTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PurposeRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ResearchItemTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.SampleTypeInfoRequest;

/**
 * Request-to-embeddable conversions shared by every act subtype's own
 * mapper (via {@code uses = ActEmbeddedMapper.class}) — every subtype reuses
 * the same 8 embeddable shapes, so this is the one place each conversion is
 * written instead of 6 times.
 */
@Mapper(componentModel = "spring")
public interface ActEmbeddedMapper {

    Institution toInstitution(InstitutionRequest request);

    EmployeeInfo toEmployeeInfo(EmployeeInfoRequest request);

    @Mapping(target = "description.uz", source = "descriptionUz")
    @Mapping(target = "description.ru", source = "descriptionRu")
    ConditionInfo toConditionInfo(ConditionInfoRequest request);

    PackageTypeInfo toPackageTypeInfo(PackageTypeInfoRequest request);

    ConservationTypeInfo toConservationTypeInfo(ConservationTypeInfoRequest request);

    Purpose toPurpose(PurposeRequest request);

    ResearchItemTypeInfo toResearchItemTypeInfo(ResearchItemTypeInfoRequest request);

    SampleTypeInfo toSampleTypeInfo(SampleTypeInfoRequest request);
}
