package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Form089;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm089;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.Form089Service;

import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.FORM089;

@RestController
@RequestMapping(FORM089)
@Tag(name = "Form 089")
@RequiredArgsConstructor
public class Form089Controller {
    final Form089Service form089Service;

    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form089> create(@Valid @RequestBody ReqForm089 reqForm089, @CurrentUser User user) {
        return form089Service.createForm089(reqForm089, user);
    }

    @PutMapping("/{id}")
    public ApiResponse<Form089> update(
            @PathVariable int id,
            @Valid @RequestBody ReqForm089 reqForm089,
            @CurrentUser User user
    ) {
        return form089Service.updateForm089(id, reqForm089, user);
    }
}
