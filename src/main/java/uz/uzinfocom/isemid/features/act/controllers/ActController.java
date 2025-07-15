package uz.uzinfocom.isemid.features.act.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

@RestController
@RequestMapping(ApiPaths.Act.ROOT)
@RequiredArgsConstructor
@Tag(name = "Act")
public class ActController {

    private final ActService actService;

    @Operation(
            summary = "Получить список актов по Card ID",
            description = "Возвращает список всех актов, связанных с указанным Card ID. Только авторизованные пользователи имеют доступ."
    )
    @GetMapping("/by-card/{cardId}")
    public ApiResponse<?> getAllActsByCardId(@PathVariable Long cardId) {
        return actService.getAllByCardId(cardId);
    }

    @Operation(summary = "Получить данные акта по ID",
            description = "Извлекает подробную информацию о конкретном акте по его уникальному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<?> getActById(@PathVariable long id) {
        return actService.getById(id);
    }

    @Operation(summary = "Обновить данные акта",
            description = "Обновляет информацию о конкретном акте. Только авторизованные пользователи могут вносить изменения.")
    @PutMapping("/{id}")
    public ApiResponse<?> updateAct(
            @PathVariable long id,
            @RequestBody ActRequest actRequest,
            @CurrentUser User currentUser
    ) {
        return actService.updateAct(id, actRequest, currentUser);
    }

    @Operation(summary = "Удалить акт по ID",
            description = "Удаляет акт с указанным идентификатором. Только уполномоченные пользователи имеют право на удаление.")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable Long id) {
        return actService.deleteById(id);
    }
}

