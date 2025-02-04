package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.JOURNAL_060;

@RestController
@RequestMapping(JOURNAL_060)
@Tag(name = "Journal 060", description = "")
@RequiredArgsConstructor
public class Journal060Controller {

}
