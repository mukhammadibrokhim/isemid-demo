package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.LegalEntityService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.LEGAL_ENTITY;

@RestController
@RequestMapping(LEGAL_ENTITY)
@Tag(name = "Legal entity")
@RequiredArgsConstructor
public class LegalEntityController {

    final LegalEntityService legalEntityService;

    @GetMapping
    public ApiResponse<?> getLegalEntity(
            @RequestParam String tin,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token

    ) {
        return legalEntityService.fetchLegalEntityByTin(tin, token);
    }
}
