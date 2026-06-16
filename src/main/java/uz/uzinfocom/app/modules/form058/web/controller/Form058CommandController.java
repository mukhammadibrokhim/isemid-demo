package uz.uzinfocom.app.modules.form058.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.features.form058.application.command.delete.DeleteForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.cancel.CancelForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Service;
import uz.uzinfocom.app.modules.form058.web.mapper.Form058WebMapper;
import uz.uzinfocom.app.modules.form058.web.request.ApproveForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.AssignCardRequest;
import uz.uzinfocom.app.modules.form058.web.request.CancelForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.UpdateForm058Request;
import uz.uzinfocom.app.modules.form058.web.response.CreateForm058Response;
import uz.uzinfocom.app.modules.form058.web.response.UpdateForm058Response;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058CommandController {

    private final CreateForm058Service createForm058Service;
    private final UpdateForm058Service updateForm058Service;
    private final DeleteForm058Service deleteForm058Service;
    private final CancelForm058Service cancelForm058Service;
    private final ApproveForm058Service approveForm058Service;
    private final Form058WebMapper form058WebMapper;
    private final MessageResolver messageResolver;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CreateForm058Response> create(@Valid @RequestBody CreateForm058Request request) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form058WebMapper.toResponse(createForm058Service.create(form058WebMapper.toCommand(request)))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(updateForm058Service.update(form058WebMapper.toCommand(id, request)))
        );
    }

    @PostMapping("/assign-card")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> assignCard(@Valid @RequestBody AssignCardRequest request) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(updateForm058Service.assignCard(request.formId(), request.cardId()))
        );
    }

    @PatchMapping("/{formId}/change-received-org/{orgId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> changeReceiver(
            @PathVariable @Positive Long formId,
            @PathVariable UUID orgId
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(updateForm058Service.changeReceiver(formId, orgId))
        );
    }

    @PatchMapping("/{formId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> cancel(
            @PathVariable @Positive Long formId,
            @Valid @RequestBody CancelForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(cancelForm058Service.cancel(form058WebMapper.toCommand(formId, request)))
        );
    }

    @PatchMapping("/{formId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> approve(
            @PathVariable @Positive Long formId,
            @Valid @RequestBody ApproveForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(approveForm058Service.approve(form058WebMapper.toCommand(formId, request)))
        );
    }

    @PutMapping("/{formId}/not-approve")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> notApprove(
            @PathVariable @Positive Long formId,
            @Valid @RequestBody(required = false) CancelForm058Request request
    ) {
        String reason = request == null ? null : request.reason();
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(approveForm058Service.notApprove(formId, reason))
        );
    }

    @PutMapping("/{formId}/approve-diagnosis")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> approveDiagnosis(
            @PathVariable @Positive Long formId,
            @Valid @RequestBody ApproveForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(
                        approveForm058Service.approveDiagnosis(form058WebMapper.toCommand(formId, request))
                )
        );
    }

    @DeleteMapping("/{formId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(@PathVariable @Positive Long formId) {
        deleteForm058Service.delete(formId);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
