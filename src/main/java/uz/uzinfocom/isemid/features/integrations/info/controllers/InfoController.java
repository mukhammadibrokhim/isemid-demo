package uz.uzinfocom.isemid.features.integrations.info.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that provides information APIs for external systems (integrations).
 * <p>
 * This API allows external systems to retrieve information about patients
 * and other relevant data from the system.
 * </p>
 */
@RestController
@RequestMapping
@Tag(name = "Info System", description = "APIs to provide system information to external integrations")
@RequiredArgsConstructor
public class InfoController {

}
