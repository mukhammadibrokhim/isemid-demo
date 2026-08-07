package uz.uzinfocom.app.platform.devmonitoring.application.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of open {@code /v1/dev/stream} connections and the single place
 * that pushes named SSE events to all of them. One connection is shared by
 * every event type (see {@link uz.uzinfocom.app.shared.constants.api.ApiPaths.Dev#STREAM})
 * rather than one connection per data source, so a dev panel tab never needs
 * more than one open connection to this origin.
 */
@Slf4j
@Component
public class DevSseBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(throwable -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    public boolean hasSubscribers() {
        return !emitters.isEmpty();
    }

    public void broadcast(String eventName, Object payload) {
        if (emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException sendFailure) {
                emitters.remove(emitter);
                emitter.completeWithError(sendFailure);
            }
        }
    }
}
