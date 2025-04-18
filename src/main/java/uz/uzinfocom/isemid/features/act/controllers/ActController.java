package uz.uzinfocom.isemid.features.act.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

@RestController
@RequestMapping(ApiPaths.Act.ROOT)
@RequiredArgsConstructor
@Tag(name = "Act")
public class ActController {

    private final ActService actService;

    @Operation(summary = "Get act details by ID",
            description = "Fetches details of a specific act using its unique identifier.")
    @GetMapping("/{id}")
    public ApiResponse<?> getActById(@PathVariable long id) {
        return actService.getById(id);
    }

    @Operation(summary = "Update act details",
            description = "Updates the details of a specific act. Only authorized users can modify the act.")
    @PutMapping("/{id}")
    public ApiResponse<?> updateAct(
            @PathVariable long id,
            @RequestBody ActRequest actRequest,
            @CurrentUser User currentUser
    ) {
        return actService.updateAct(id, actRequest, currentUser);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable Long id) {
        return actService.deleteById(id);
    }

}

