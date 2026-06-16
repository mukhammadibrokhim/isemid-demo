package uz.uzinfocom.app.modules.form058.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.application.query.Form058QueryService;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResult;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.web.mapper.Form058WebMapper;
import uz.uzinfocom.app.modules.form058.web.response.Form058DetailedResponse;
import uz.uzinfocom.app.modules.form058.web.response.Form058TableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058QueryController {

    private final Form058QueryService form058QueryService;
    private final Form058WebMapper form058WebMapper;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form058TableResponse> all(
            @ParameterObject @Valid @ModelAttribute Form058Filter filter,
            HttpServletRequest httpRequest
    ) {
        Page<Form058TableResponse> page = form058QueryService.findSent(filter)
                .map(form058WebMapper::toResponse);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping("/received/all")
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form058TableResponse> received(
            @ParameterObject @Valid @ModelAttribute Form058Filter filter,
            HttpServletRequest httpRequest
    ) {
        Page<Form058TableResponse> page = form058QueryService.findReceived(filter)
                .map(form058WebMapper::toResponse);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping("/by-nnuzb")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailedResponse> byNnuzb(@RequestParam @NotBlank String nnuzb) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058WebMapper.toResponse(form058QueryService.getByNnuzb(nnuzb))
        );
    }

    @GetMapping("/confirmation-stats")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<FormStatus, Long>> confirmationStats() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.confirmationStats()
        );
    }

    @GetMapping("/by-card/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailedResponse> byCard(@PathVariable @Positive Long cardId) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058WebMapper.toResponse(form058QueryService.getByCard(cardId))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailedResponse> byId(@PathVariable @Positive Long id) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058WebMapper.toResponse(form058QueryService.getById(id))
        );
    }
}
