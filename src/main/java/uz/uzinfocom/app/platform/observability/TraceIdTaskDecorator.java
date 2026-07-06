package uz.uzinfocom.app.platform.observability;

import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TraceIdTaskDecorator implements TaskDecorator {

    @Override
    public @NonNull Runnable decorate(@NonNull Runnable task) {
        String callerTraceId = MDC.get(TraceContext.MDC_KEY);

        return () -> {
            String workerPreviousTraceId =
                    MDC.get(TraceContext.MDC_KEY);

            try {
                if (StringUtils.hasText(callerTraceId)) {
                    MDC.put(
                            TraceContext.MDC_KEY,
                            callerTraceId
                    );
                } else {
                    MDC.remove(TraceContext.MDC_KEY);
                }

                task.run();
            } finally {
                /*
                 * Thread pool threadlari qayta ishlatiladi.
                 * Eski request trace IDsi keyingi taskka o‘tmasligi kerak.
                 */
                if (StringUtils.hasText(workerPreviousTraceId)) {
                    MDC.put(
                            TraceContext.MDC_KEY,
                            workerPreviousTraceId
                    );
                } else {
                    MDC.remove(TraceContext.MDC_KEY);
                }
            }
        };
    }
}