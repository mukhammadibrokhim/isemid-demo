package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.act.ActService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ACT;

@RestController
@RequestMapping(ACT)
@RequiredArgsConstructor
@Tag(name = "Act")
public class ActController {

    private final ActService actService;

    @Operation(summary = "Update act details",
            description = "Updates the details of a specific act. Only authorized users can modify the act.")
    @PutMapping("/{id}")
    public ApiResponse<?> updateAct(
            @PathVariable long id,
            @RequestBody ReqAct reqAct,
            @CurrentUser User currentUser
    ) {
        return actService.updateAct(id, reqAct, currentUser);
    }
    @Operation(summary = "Get act details by ID",
            description = "Fetches details of a specific act using its unique identifier.")
    @GetMapping("/{id}")
    public ApiResponse<?> getActById(@PathVariable long id) {
        return actService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable Long id) {
        return actService.deleteById(id);
    }

}

