package uz.uzinfocom.app.orchestration.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Per-user unread-count push, same shape as {@code ExportJobService.streamProgress}/
 * {@code pushProgress}: a single-thread scheduler polls the DB every second and pushes
 * an SSE event, closing quietly on client disconnect rather than routing the
 * {@code IOException}/{@code IllegalStateException} through MVC's normal error-response
 * machinery (which fails trying to write a JSON body onto an already-committed
 * {@code text/event-stream} response).
 */
@Service
@RequiredArgsConstructor
public class NotificationStreamService {

    private static final long SSE_TIMEOUT_MILLIS = 15 * 60 * 1000L;

    private final NotificationRepository notificationRepository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "notification-stream-sse");
        thread.setDaemon(true);
        return thread;
    });

    public SseEmitter stream(Long currentUserId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];

        futureHolder[0] = scheduler.scheduleAtFixedRate(
                () -> pushUnreadCount(currentUserId, emitter),
                0, 1, TimeUnit.SECONDS
        );

        emitter.onCompletion(() -> futureHolder[0].cancel(false));
        emitter.onTimeout(() -> {
            futureHolder[0].cancel(false);
            emitter.complete();
        });
        emitter.onError(exception -> futureHolder[0].cancel(false));

        return emitter;
    }

    private void pushUnreadCount(Long currentUserId, SseEmitter emitter) {
        try {
            long unreadCount = notificationRepository.countByRecipientUserIdAndReadFalse(currentUserId);
            emitter.send(SseEmitter.event().name("unread-count").data(unreadCount));
        } catch (IOException | IllegalStateException clientGone) {
            emitter.complete();
        }
    }
}
