package uz.uzinfocom.isemid.features.forecast.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.service.Form058HoltWeeklyForecastFacade;
import uz.uzinfocom.isemid.features.forecast.service.Form058HoltWintersFacade;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

@RestController
@Tag(name = "Forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final Form058HoltWeeklyForecastFacade facade;
    private final Form058HoltWintersFacade facadeV2;
    private final MessageUtil messageUtil;

    @GetMapping(ApiPaths.Forecast.ROOT_V2 + "/holt-weekly")
    public ApiResponse<?> holtWeekly(
            @CurrentUser User currentUser,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("alpha") double alpha,
            @RequestParam("beta") double beta,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String mkb10C0de
    ) {
        return ApiResponse.success(messageUtil,
                facade.holt(currentUser, startDate, endDate, alpha, beta, stateCode, mkb10C0de));
    }

    @GetMapping(ApiPaths.Forecast.ROOT + "/holt-weekly")
    public ApiResponse<?> holtWeeklyV2(
            @CurrentUser User currentUser,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("alpha") double alpha,
            @RequestParam("beta") double beta,
            @RequestParam("gamma") double gamma,
            @RequestParam(value = "season", required = false) Integer season,
            @RequestParam(value = "horizon", required = false) Integer horizon,
            @RequestParam(value = "period", defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String mkb10C0de
    ) {
        return ApiResponse.success(messageUtil,
                facadeV2.holt(currentUser, startDate, endDate,
                        alpha, beta, gamma, season, horizon, period,
                        stateCode, mkb10C0de));
    }
}
