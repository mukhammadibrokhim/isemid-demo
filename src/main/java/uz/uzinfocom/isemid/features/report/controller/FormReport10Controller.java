package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;

@RestController
@RequestMapping(ApiPaths.Report.FORM10)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class FormReport10Controller {
}
