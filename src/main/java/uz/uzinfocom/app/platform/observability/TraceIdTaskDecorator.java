package uz.uzinfocom.app.platform.observability;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TraceIdTaskDecorator implements TaskDecorator {

    private final TraceIdProvider traceIdProvider;

    @Override
    public @NonNull Runnable decorate(@NonNull Runnable task) {
        String callerTraceId = TraceContext.currentTraceId();
        String taskTraceId = StringUtils.hasText(callerTraceId)
                ? callerTraceId
                : traceIdProvider.generateTraceId();

        return () -> {
            try (TraceContext.Scope ignored = TraceContext.open(taskTraceId)) {
                task.run();
            }
        };
    }
}
