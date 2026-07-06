package uz.uzinfocom.app.platform.iam.application.organization.query.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.domain.enums.ServiceType;

import java.util.List;
import java.util.UUID;

@Schema(description = "Детальная информация об организации.")
public record OrganizationDetailResponse(

        @Schema(description = "Уникальный идентификатор организации.", example = "1")
        Long id,

        @Schema(description = "Уникальный UUID организации.")
        UUID uuid,

        @Schema(description = "ИНН организации.", example = "123456789")
        String tin,

        @Schema(description = "Наименование организации.")
        String name,

        @Schema(description = "Признак активности организации.", example = "true")
        Boolean active,

        @Schema(description = "Контактный номер телефона организации.",
                example = "+998901234567")
        String phone,

        @Schema(description = "Код региона организации.", example = "UZ-AN")
        String regionCode,

        @Schema(description = "Код района или города организации.",
                example = "AN-001")
        String districtCode,

        @Schema(description = "Уровень организации.")
        OrganizationLevel levelType,

        @Schema(description = "Тип медицинской организации.")
        MedicalType medicalType,

        @Schema(description = "Виды услуг организации.")
        List<ServiceType> serviceTypes,

        @Schema(description = "Информация о создании и последнем изменении записи.")
        AuditResponse audit
) {
}