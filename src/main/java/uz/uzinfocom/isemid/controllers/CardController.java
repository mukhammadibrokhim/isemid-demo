package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.CardService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CARD;

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
}
