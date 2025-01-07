package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.services.CitizenService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CITIZEN;

@RestController
@RequestMapping(CITIZEN)
@Tag(name = "Citizen", description = "Fetch citizen data from GCP if citizen not found in db")
@RequiredArgsConstructor
public class CitizenController {

    final CitizenService citizenService;

    @Operation(
            summary = "Retrieve citizen information",
            description = """
                        Retrieves citizen information based on provided parameters:
                        - If `nnuzb` and `photo` are provided, retrieves citizen by their unique nnuzb ID and photo.
                        - If `series` and `number` are provided, retrieves citizen by child details.
                    """,
            parameters = {
                    @Parameter(name = "nnuzb", description = "Unique nnuzb ID of the citizen"),
                    @Parameter(name = "photo", description = "Photo associated with the citizen"),
                    @Parameter(name = "series", description = "Series of the child's document"),
                    @Parameter(name = "number", description = "Number of the child's document")
            })
    @GetMapping
    public ResponseEntity<Object> getCitizen(
            @RequestParam(required = false) String nnuzb,
            @RequestParam(required = false) String photo,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token

    ) {
        if (nnuzb != null && photo != null) {
            return ResponseEntity.ok(citizenService.getCitizenByNnuzb(nnuzb, photo, token));
        } else if (series != null && number != null) {
            return ResponseEntity.ok(citizenService.getCitizenByChild(series, number, token));
        } else throw new ApiException(BAD_REQUEST, "Invalid parameters");
    }
}
