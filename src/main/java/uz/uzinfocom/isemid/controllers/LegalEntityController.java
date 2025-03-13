package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.LegalEntityService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.LEGAL_ENTITY;

@RestController
@RequestMapping(LEGAL_ENTITY)
@Tag(name = "Legal entity")
@RequiredArgsConstructor
public class LegalEntityController {

    final LegalEntityService legalEntityService;

    @GetMapping
    public ApiResponse<?> getLegalEntity(@RequestParam String tin) {
        return legalEntityService.fetchLegalEntityByTin(tin);
    }
}
