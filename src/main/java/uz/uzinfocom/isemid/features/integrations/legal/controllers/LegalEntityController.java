package uz.uzinfocom.isemid.features.integrations.legal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.integrations.legal.service.LegalEntityService;

@RestController
@RequestMapping(ApiPaths.LegalEntity.ROOT)
@Tag(name = "Legal entity", description = "API для получения информации о юридическом лице по ИНН")
@RequiredArgsConstructor
public class LegalEntityController {

    private final LegalEntityService legalEntityService;
    @Operation(
            summary = "Получение информации о юридическом лице",
            description = "Возвращает данные юридического лица по заданному ИНН (Tax Identification Number).")
    @GetMapping
    public ApiResponse<?> getLegalEntity(@RequestParam String tin) {
        return legalEntityService.fetchLegalEntityByTin(tin);
    }
}
