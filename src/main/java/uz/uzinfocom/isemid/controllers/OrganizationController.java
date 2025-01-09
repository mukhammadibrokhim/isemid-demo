package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.OrganizationService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ORGANIZATION;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
@RestController
@RequestMapping(ORGANIZATION)
@Tag(name = "Организация", description = "API для работы с организациями")
@RequiredArgsConstructor
public class OrganizationController {
    final OrganizationService organizationService;

    @Operation(
            summary = "Получить все организации",
            description = "Получает список всех организаций с возможностью пагинации.",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации"),
                    @Parameter(name = "size", description = "Количество элементов на странице для пагинации"),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/all")
    public ApiResponsePage getAllOrganizations(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            HttpServletRequest request
    ) {
        return organizationService.getAllOrganizationsByFilter(page, size, request);
    }

    @Operation(
            summary = "Получить организацию по ID",
            description = "Получает организацию по заданному идентификатору (ID).",
            parameters = {
                    @Parameter(name = "id", description = "ID организации", required = true)
            })
    @GetMapping("/{id}")
    public ApiResponse<Organization> getOrganizationById(@PathVariable long id) {
        return organizationService.getById(id);
    }

    @Operation(
            summary = "Обновить организацию из MIS",
            description = "Обновляет информацию об организации, используя данные из MIS по UUID организации.",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID организации для обновления", required = true),
                    @Parameter(name = "Authorization", description = "JWT токен авторизации", required = true)
            })
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<Organization> updateOrganization(
            @PathVariable UUID uuid,
            @CurrentUser User currentUser,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return organizationService.updateUserFromMis(uuid, currentUser, token);
    }
}
