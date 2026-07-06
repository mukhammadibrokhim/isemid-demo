package uz.uzinfocom.app.platform.iam.web.user.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.util.List;

@Schema(
        name = "UserFilterRequest",
        description = "Параметры фильтрации, сортировки и пагинации списка пользователей."
)
public record UserFilterRequest(

        @Schema(
                description = "Номер страницы. Нумерация начинается с 1.",
                example = "1",
                defaultValue = "1",
                minimum = "1"
        )
        @Min(value = 1, message = "{user.filter.page.min}")
        Integer page,

        @Schema(
                description = "Количество записей на одной странице.",
                example = "20",
                defaultValue = "20",
                minimum = "1",
                maximum = "200"
        )
        @Min(value = 1, message = "{user.filter.size.min}")
        @Max(value = 200, message = "{user.filter.size.max}")
        Integer size,

        @Schema(
                description = "Поле, по которому выполняется сортировка.",
                example = "lastName"
        )
        String sortBy,

        @Schema(
                description = "Направление сортировки.",
                example = "asc",
                defaultValue = "asc",
                allowableValues = {"asc", "desc"}
        )
        String sortDir,

        @Schema(
                description = "Фильтр по имени пользователя. Поддерживается частичное совпадение без учета регистра.",
                example = "Иван"
        )
        String firstName,

        @Schema(
                description = "Фильтр по фамилии пользователя. Поддерживается частичное совпадение без учета регистра.",
                example = "Иванов"
        )
        String lastName,

        @Schema(
                description = "Фильтр по отчеству пользователя. Поддерживается частичное совпадение без учета регистра.",
                example = "Иванович"
        )
        String middleName,

        @Schema(
                description = "Фильтр по ННУЗБ пользователя. Выполняется точное совпадение.",
                example = "12345678901234"
        )
        String nnuzb,

        @Schema(
                description = "Фильтр по номеру телефона пользователя. Поддерживается частичное совпадение.",
                example = "998901234567"
        )
        String phoneNumber,

        @Schema(
                description = "Фильтр по признаку активности пользователя.",
                example = "true"
        )
        Boolean active,

        @Schema(
                description = "Код региона организации пользователя.",
                example = "UZ-TK"
        )
        String organizationRegionCode,

        @Schema(
                description = "Код района или города организации пользователя.",
                example = "TK-269"
        )
        String organizationDistrictCode,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "Идентификаторы ролей пользователя. "
                                + "Пользователь должен иметь хотя бы одну из указанных ролей."
                ),
                schema = @Schema(
                        description = "Идентификатор роли.",
                        example = "1",
                        minimum = "1"
                )
        )
        List<
                @Positive(message = "{user.filter.role-id.positive}")
                        Long
                > roleIds,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "Медицинские типы организаций пользователя. "
                                + "Организация должна иметь хотя бы один из указанных типов."
                ),
                schema = @Schema(
                        description = "Медицинский тип организации.",
                        implementation = MedicalType.class,
                        example = "SANEPID_SERVICE"
                )
        )
        List<MedicalType> organizationMedicalTypes

) implements PageableRequest {
}