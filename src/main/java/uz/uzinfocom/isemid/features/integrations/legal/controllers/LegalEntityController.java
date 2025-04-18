package uz.uzinfocom.isemid.features.integrations.legal.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.integrations.legal.service.LegalEntityService;

@RestController
@RequestMapping(ApiPaths.LegalEntity.ROOT)
@Tag(name = "Legal entity")
@RequiredArgsConstructor
public class LegalEntityController {

    private final LegalEntityService legalEntityService;

    @GetMapping
    public ApiResponse<?> getLegalEntity(@RequestParam String tin) {
        return legalEntityService.fetchLegalEntityByTin(tin);
    }

}
