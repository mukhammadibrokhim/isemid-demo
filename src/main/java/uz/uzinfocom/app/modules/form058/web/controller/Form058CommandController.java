package uz.uzinfocom.app.modules.form058.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.delete.DeleteForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Service;
import uz.uzinfocom.app.modules.form058.web.dto.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.DeleteForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.UpdateForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.response.CreateForm058Response;
import uz.uzinfocom.app.modules.form058.web.dto.response.UpdateForm058Response;
import uz.uzinfocom.app.modules.form058.web.mapper.Form058WebMapper;
import uz.uzinfocom.app.modules.form058.web.resolvers.Form058Headers;
import uz.uzinfocom.app.modules.form058.web.resolvers.Form058SourceResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Form 058")
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058CommandController {

    private final CreateForm058Service createForm058Service;
    private final UpdateForm058Service updateForm058Service;
    private final DeleteForm058Service deleteForm058Service;
    private final Form058WebMapper form058WebMapper;
    private final Form058SourceResolver sourceResolver;
    private final MessageResolver messageResolver;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CreateForm058Response> create(
            @RequestHeader(value = Form058Headers.X_SOURCE, required = false) String sourceHeader,
            @Valid @RequestBody CreateForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form058WebMapper.toResponse(createForm058Service.create(form058WebMapper.toCommand(request, sourceResolver.resolve(sourceHeader))))
        );
    }

    @PutMapping(ApiPaths.Form058.BY_ID)
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

    @DeleteMapping(value = ApiPaths.Form058.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @Valid @RequestBody DeleteForm058Request request
    ) {
        deleteForm058Service.delete(id, request.reason());

        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
