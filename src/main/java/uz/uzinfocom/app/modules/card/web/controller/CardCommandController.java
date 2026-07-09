package uz.uzinfocom.app.modules.card.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.card.application.command.CardCommandService;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.web.dto.request.AssignActRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRejectRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.ReassignCardUsersRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

/**
 * Exclusively {@code /v1/cards/*} — anything triggered from a Form058 path
 * (create, bulk-assign) lives in {@link Form058CardCommandController}
 * instead, so this stays a pure "Card" tag in Swagger.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Card.ROOT)
@Tag(name = "Card")
public class CardCommandController {

    private final CardCommandService cardCommandService;
    private final MessageResolver messageResolver;

    @PutMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CardDetailResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRequest request
    ) {
        CardDetailResponse updated = cardCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), updated);
    }

    @DeleteMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        cardCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }

    @PatchMapping(ApiPaths.Card.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> accept(@PathVariable @Positive Long id) {
        cardCommandService.acceptByUser(id);
        return ApiResponse.success(messageResolver.resolve("common.accepted"));
    }

    @PatchMapping(ApiPaths.Card.REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> reject(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRejectRequest request
    ) {
        cardCommandService.rejectByUser(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    @PatchMapping(ApiPaths.Card.COMPLETE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> complete(@PathVariable @Positive Long id) {
        cardCommandService.complete(id);
        return ApiResponse.success(messageResolver.resolve("common.completed"));
    }

    @PatchMapping(ApiPaths.Card.SUPERVISOR_APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorApprove(@PathVariable @Positive Long id) {
        cardCommandService.approveBySupervisor(id);
        return ApiResponse.success(messageResolver.resolve("common.approved"));
    }

    @PatchMapping(ApiPaths.Card.SUPERVISOR_REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReject(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRejectRequest request
    ) {
        cardCommandService.rejectBySupervisor(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    /**
     * Only the supervisor a card is already assigned to may use this —
     * only valid after the attached user rejected the assignment, hands
     * the card to different employee(s), and resets it to NEW.
     */
    @PatchMapping(ApiPaths.Card.SUPERVISOR_REASSIGN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReassign(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReassignCardUsersRequest request
    ) {
        cardCommandService.reassignUsers(id, request);
        return ApiResponse.success(messageResolver.resolve("common.reassigned"));
    }

    @PostMapping(ApiPaths.Card.ACTS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> assignAct(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignActRequest request
    ) {
        cardCommandService.assignAct(id, request.actType());
        return ApiResponse.success(messageResolver.resolve("common.created"));
    }
}
