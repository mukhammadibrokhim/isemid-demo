package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.payload.requests.ReqJournal060;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.Journal060Service;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.JOURNAL_060;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(JOURNAL_060)
@Tag(name = "AJournal 060", description = "Journal 060 API")
@RequiredArgsConstructor
public class Journal060Controller {
    final Journal060Service journal060Service;

    @GetMapping("/all")
    public ApiResponsePage getAllJournal060(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            HttpServletRequest request
    ) {
        return journal060Service.getAllJournal060Filter(
                page,
                size,
                getFullURL(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getJournal060ById(@PathVariable int id) {
        return journal060Service.getById(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateJournal060(@PathVariable int id, @RequestBody ReqJournal060 reqJournal060) {
        return journal060Service.update(id, reqJournal060);
    }
}
