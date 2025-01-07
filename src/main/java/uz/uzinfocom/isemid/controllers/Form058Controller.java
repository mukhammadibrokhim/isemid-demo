package uz.uzinfocom.isemid.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm058;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.Form058Service;

import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.FORM0058;

@RestController
@RequestMapping(FORM0058)
@Tag(name = "Form 058")
@RequiredArgsConstructor
public class Form058Controller {

    final Form058Service form058Service;

    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form058> create(@Valid @RequestBody ReqForm058 reqForm058, @CurrentUser User user) {
        return form058Service.createForm058(reqForm058, user);
    }

    @PutMapping("/{id}")
    public ApiResponse<Form058> update(
            @RequestBody ReqForm058 reqForm058,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return form058Service.updateForm058(reqForm058, id, user);
    }
}


