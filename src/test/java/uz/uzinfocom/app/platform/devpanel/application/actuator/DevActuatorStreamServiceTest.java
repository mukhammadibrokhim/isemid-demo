package uz.uzinfocom.app.platform.devpanel.application.actuator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevActuatorStreamServiceTest {

    private final DevActuatorInvoker invoker = mock(DevActuatorInvoker.class);
    private final DevActuatorStreamService service = new DevActuatorStreamService(invoker);

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    private void open(String endpoint) {
        service.open(endpoint, "", new LinkedMultiValueMap<>(), null, false, SecurityContext.NONE);
    }

    @Test
    void intervalDefaultsAndIsClampedToTheAllowedRange() {
        assertThat(service.clampInterval("metrics", null)).isEqualTo(3_000);
        assertThat(service.clampInterval("metrics", 10L)).isEqualTo(1_000);
        assertThat(service.clampInterval("metrics", 2_500L)).isEqualTo(2_500);
        assertThat(service.clampInterval("metrics", 9_999_999L)).isEqualTo(60_000);
    }

    @Test
    void heavyEndpointsGetALongerMinimumInterval() {
        assertThat(service.clampInterval("threaddump", 1_000L)).isEqualTo(5_000);
        assertThat(service.clampInterval("ThreadDump", null)).isEqualTo(5_000);
        assertThat(service.clampInterval("beans", 8_000L)).isEqualTo(8_000);
    }

    @Test
    void aBadEndpointFailsTheRequestAndDoesNotLeakAStreamSlot() {
        when(invoker.get(eq("nope"), any(), any(), any(), any())).thenThrow(new NotFoundException("dev.actuator.endpoint-not-found", "nope"));

        assertThatThrownBy(() -> open("nope")).isInstanceOf(NotFoundException.class);

        assertThat(service.openStreams()).isZero();
    }

    @Test
    void streamCountIsCappedAndTheCapIsReportedAsTooManyRequests() {
        when(invoker.get(any(), any(), any(), any(), any())).thenReturn(new DevActuatorResult(200, null, "ok"));

        for (int i = 0; i < DevActuatorStreamService.MAX_STREAMS; i++) {
            open("metrics");
        }
        assertThat(service.openStreams()).isEqualTo(DevActuatorStreamService.MAX_STREAMS);

        assertThatThrownBy(() -> open("metrics")).isInstanceOfSatisfying(DevActuatorException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOO_MANY_REQUESTS));
        assertThat(service.openStreams()).isEqualTo(DevActuatorStreamService.MAX_STREAMS);
    }

    @Test
    void firstSampleIsTakenSynchronouslyWithTheCallersSecurityContext() {
        SecurityContext caller = SecurityContext.NONE;
        when(invoker.get(eq("health"), eq(""), any(), eq(List.of()), eq(caller)))
                .thenReturn(new DevActuatorResult(200, null, "ok"));

        service.open("health", "", new LinkedMultiValueMap<>(), 2_000L, true, caller);

        assertThat(service.openStreams()).isEqualTo(1);
    }
}
