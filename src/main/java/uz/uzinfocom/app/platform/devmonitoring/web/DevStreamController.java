package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.devmonitoring.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Dev Monitoring - Stream",
        description = "Single multiplexed SSE connection for the dev panel. Emits named events: "
                + "'system' and 'http' (periodic metrics snapshots, see Dev Monitoring - Metrics) and "
                + "'error' (pushed the moment a new row lands in dev_error_log, see Dev Monitoring - Errors)."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevStreamController {

    private final DevSseBroadcaster devSseBroadcaster;

    @GetMapping(ApiPaths.Dev.STREAM)
    public SseEmitter stream() {
        return devSseBroadcaster.register();
    }
}
