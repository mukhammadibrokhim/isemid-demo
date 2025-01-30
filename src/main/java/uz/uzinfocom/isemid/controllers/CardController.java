package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.enums.CardStatus;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.CardService;

import java.util.UUID;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CARD;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(CARD)
@RequiredArgsConstructor
@Tag(name = "Card")
public class CardController {

    final CardService cardService;


    @PostMapping("/assign-user")
    public ApiResponse<?> assignUser(
    ) {
        return null;
    }

    @GetMapping("/all")
    public ApiResponse<?> allByFilter(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String formTypeCode,
            @RequestParam(required = false) UUID fromOrgId,
            @RequestParam(required = false) UUID toOrgId,
            @RequestParam(required = false) CardStatus cardStatus,
            HttpServletRequest request
    ) {
        return cardService.getAllCardsByFilter(page, size, formTypeCode, fromOrgId, toOrgId, cardStatus, getFullURL(request));
    }
}
