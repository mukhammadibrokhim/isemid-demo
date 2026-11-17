package uz.uzinfocom.app.integration.inbound.common.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationLookupRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationLookupResponse;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.List;

/**
 * Lists the organizations a submitter may address a form058 / form058-1 /
 * form129 to. The create validators of all three only accept an active
 * {@code SANEPID_SERVICE} receiver, so this is the same set, pre-filtered.
 */
@Tag(
        name = "Integration - Receiver Organizations",
        description = "Список организаций-получателей, которым можно адресовать формы №058, №058-1 и №129."
)
@RestController
@RequestMapping(ApiPaths.Integration.RECEIVER_ORGANIZATIONS)
@RequiredArgsConstructor
public class InboundReceiverOrganizationController {

    private final OrganizationQueryService organizationQueryService;

    @Operation(
            summary = "Получить организации-получатели",
            description = "Возвращает активные организации. По умолчанию (без medicalTypes) - типа SANEPID_SERVICE, "
                    + "допустимые значения receiverOrganizationId при отправке форм №058, №058-1 и №129; "
                    + "с medicalTypes=HOSPITAL и т.п. - места госпитализации (hospitalPlaceId). "
                    + "Требуется любой из scope: "
                    + "form058:submit, form0581:submit, form129:submit."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<OrganizationLookupResponse> list(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Parameter(description = "Поиск по наименованию организации.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Код региона.")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района.")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Типы медицинской организации (можно несколько). Для получателей форм - "
                    + "SANEPID_SERVICE; для мест госпитализации (hospitalPlaceId) - типы стационаров, "
                    + "например HOSPITAL, PEDIATRIC_HOSPITAL, EMERGENCY_HOSPITAL. "
                    + "Если не указан - SANEPID_SERVICE.")
            @RequestParam(required = false) List<MedicalType> medicalTypes,
            @Parameter(description = "Максимум записей (1-50, по умолчанию 20).")
            @RequestParam(required = false) Integer limit
    ) {
        InboundCallerContext.requireAnyScope(
                IntegrationScope.FORM058_SUBMIT, IntegrationScope.FORM0581_SUBMIT, IntegrationScope.FORM129_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(source);

        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 50);
        List<MedicalType> types = medicalTypes == null || medicalTypes.isEmpty()
                ? List.of(MedicalType.SANEPID_SERVICE)
                : medicalTypes;
        return organizationQueryService.lookup(new OrganizationLookupRequest(
                search, null, null, types, null, true,
                regionCode, districtCode, safeLimit));
    }
}
