package uz.uzinfocom.isemid.features.act.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.services.ActService;

import java.util.UUID;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Act.ROOT)
@RequiredArgsConstructor
@Tag(name = "Act")
public class ActController {
    private final ActService actService;
    private final MessageUtil messageUtil;
    private final ActMapperFactory factory;

    @GetMapping("/all")
    public PagedResponse<ActResponse> getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam Long cardId,
            HttpServletRequest request
    ) {
        Page<Act> actPage = actService.filter(page, size, cardId);
        return PagedResponse.build(page, actPage.map(factory::toResponse), buildRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получить данные акта по ID", description = "Извлекает подробную информацию о конкретном акте по его уникальному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<?> getActById(@PathVariable long id) {
        return ApiResponse.success(messageUtil, factory.toResponse(actService.getById(id)));
    }

    @Operation(summary = "Обновить данные акта", description = "Обновляет информацию о конкретном акте. Только авторизованные пользователи могут вносить изменения.")
    @PutMapping("/{id}")
    public ApiResponse<?> updateAct(
            @PathVariable long id,
            @RequestBody ActRequest actRequest) {
        return ApiResponse.success(messageUtil, factory.toResponse(actService.update(id, actRequest)));
    }

    @Operation(summary = "Удалить акт по ID", description = "Удаляет акт с указанным идентификатором. Только уполномоченные пользователи имеют право на удаление.")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable Long id) {
        actService.deleteById(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }

    // ===================================
    // 📡 LIS Integration
    // ===================================

    @Operation(
            summary = "Отправить акт в LIS",
            description = "Отправляет акт (153–224) в LIS систему и возвращает статус отправки."
    )
    @PostMapping("/send-to-lis")
    public ApiResponse<?> sendActToLis(
            @RequestBody LisRequest lisRequest,
            HttpServletRequest request
    ) {
        String orgHeader = request.getHeader("Organization-id");
        UUID organizationUuid = orgHeader != null ? UUID.fromString(orgHeader) : null;

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        actService.sendToLis(lisRequest, organizationUuid, token);
        return ApiResponse.success(messageUtil, "Акт успешно отправлен в LIS.");
    }

    @Operation(
            summary = "Приём ответа от LIS",
            description = "Callback endpoint, на который LIS отправляет ответ по акту."
    )
    @PostMapping("/lis/callback/{actId}")
    public ApiResponse<?> receiveLisResponse(
            @PathVariable Long actId,
            @RequestBody Map<String,Object> body
    ) {
        actService.handleLisResponse(actId, body);
        return ApiResponse.success(messageUtil, "Ответ от LIS успешно принят.");
    }
}

