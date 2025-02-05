package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.Journal060Service;

import java.util.UUID;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.JOURNAL_060;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(JOURNAL_060)
@Tag(name = "Journal 060", description = "Journal 060 API Service")
@RequiredArgsConstructor
public class Journal060Controller {
    final Journal060Service journal060Service;

    @GetMapping("/all")
    public ApiResponsePage getAllJournal060(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String reporterFullName,
            @RequestParam(required = false) String nnuzb,
            @RequestParam(required = false) UUID senderOrgUuid,
            @RequestParam(required = false) String receiverOrgUuid,
            HttpServletRequest request
    ) {
        return journal060Service.getAllJournal060Filter(
                page,
                size,
                gender,firstName,lastName,reporterFullName,nnuzb,senderOrgUuid,receiverOrgUuid,
                getFullURL(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getJournal060ById(@PathVariable int id) {
        return journal060Service.getById(id);
    }
}
