package uz.uzinfocom.app.orchestration.notification.application;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository.UnreadCountByRecipient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Per-user unread-count push over SSE.
 *
 * <p>Previously every open stream scheduled its own once-a-second {@code count(*)}
 * on a single shared thread - N connected users meant N queries per second, more
 * than one thread can keep up with, and one user could multiply that by opening
 * any number of tabs. Now every open stream is registered here and one scheduled
 * tick counts unread notifications for all connected users in batched, grouped
 * queries, pushing an event only when a user's count actually changed. A periodic
 * heartbeat comment lets dead connections surface (and be dropped) well before
 * the SSE timeout, and each user keeps at most {@link #MAX_STREAMS_PER_USER}
 * streams - opening another one closes the oldest.
 *
 * <p>Client disconnects close quietly rather than routing the
 * {@code IOException}/{@code IllegalStateException} through MVC's normal
 * error-response machinery (which fails trying to write a JSON body onto an
 * already-committed {@code text/event-stream} response).
 */
@Slf4j
@Service
public class NotificationStreamService {

    private static final long SSE_TIMEOUT_MILLIS = 15 * 60 * 1000L;
    private static final long POLL_INTERVAL_MILLIS = 2_000L;
    private static final long HEARTBEAT_INTERVAL_MILLIS = 25_000L;
    private static final int MAX_STREAMS_PER_USER = 3;
    private static final int QUERY_BATCH_SIZE = 500;

    private final NotificationRepository notificationRepository;
    private final Map<Long, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "notification-stream-sse");
        thread.setDaemon(true);
        return thread;
    });
    private long lastHeartbeatAt = System.currentTimeMillis();

    public NotificationStreamService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.scheduler.scheduleWithFixedDelay(this::tick, POLL_INTERVAL_MILLIS, POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    public SseEmitter stream(Long currentUserId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        SseEmitter[] evicted = new SseEmitter[1];

        subscriptions.compute(currentUserId, (userId, subscription) -> {
            Subscription target = subscription != null ? subscription : new Subscription();
            target.emitters.addLast(emitter);
            if (target.emitters.size() > MAX_STREAMS_PER_USER) {
                evicted[0] = target.emitters.pollFirst();
            }
            return target;
        });

        emitter.onCompletion(() -> unregister(currentUserId, emitter));
        emitter.onTimeout(() -> {
            unregister(currentUserId, emitter);
            emitter.complete();
        });
        emitter.onError(exception -> unregister(currentUserId, emitter));

        if (evicted[0] != null) {
            evicted[0].complete();
        }

        scheduler.execute(() -> pushInitialCount(currentUserId, emitter));
        return emitter;
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    private void unregister(Long userId, SseEmitter emitter) {
        subscriptions.computeIfPresent(userId, (id, subscription) -> {
            subscription.emitters.remove(emitter);
            return subscription.emitters.isEmpty() ? null : subscription;
        });
    }

    private void pushInitialCount(Long userId, SseEmitter emitter) {
        try {
            long unreadCount = notificationRepository.countByRecipientUserIdAndReadFalse(userId);
            Subscription subscription = subscriptions.get(userId);
            if (subscription != null) {
                subscription.lastSentCount = unreadCount;
            }
            send(emitter, SseEmitter.event().name("unread-count").data(unreadCount));
        } catch (RuntimeException exception) {
            log.warn("event=notification_stream_initial_failed userId={}", userId, exception);
        }
    }

    /**
     * Must never throw: an exception escaping a {@code scheduleWithFixedDelay} task
     * silently cancels every future run, which would freeze all streams.
     */
    private void tick() {
        try {
            if (!subscriptions.isEmpty()) {
                pushChangedCounts();
                pushHeartbeatIfDue();
            }
        } catch (RuntimeException exception) {
            log.warn("event=notification_stream_tick_failed subscribers={}", subscriptions.size(), exception);
        }
    }

    private void pushChangedCounts() {
        List<Long> userIds = new ArrayList<>(subscriptions.keySet());

        for (int from = 0; from < userIds.size(); from += QUERY_BATCH_SIZE) {
            List<Long> batch = userIds.subList(from, Math.min(from + QUERY_BATCH_SIZE, userIds.size()));

            Map<Long, Long> unreadByUser = new HashMap<>();
            for (UnreadCountByRecipient row : notificationRepository.countUnreadByRecipientUserIds(batch)) {
                unreadByUser.put(row.getRecipientUserId(), row.getCount());
            }

            for (Long userId : batch) {
                Subscription subscription = subscriptions.get(userId);
                if (subscription == null) {
                    continue;
                }
                long unreadCount = unreadByUser.getOrDefault(userId, 0L);
                if (Objects.equals(subscription.lastSentCount, unreadCount)) {
                    continue;
                }
                subscription.lastSentCount = unreadCount;
                for (SseEmitter emitter : subscription.emitters) {
                    send(emitter, SseEmitter.event().name("unread-count").data(unreadCount));
                }
            }
        }
    }

    private void pushHeartbeatIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastHeartbeatAt < HEARTBEAT_INTERVAL_MILLIS) {
            return;
        }
        lastHeartbeatAt = now;

        for (Subscription subscription : subscriptions.values()) {
            for (SseEmitter emitter : subscription.emitters) {
                send(emitter, SseEmitter.event().comment("keep-alive"));
            }
        }
    }

    private void send(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException clientGone) {
            emitter.complete();
        }
    }

    private static final class Subscription {
        private final Deque<SseEmitter> emitters = new ConcurrentLinkedDeque<>();
        private volatile Long lastSentCount;
    }
}
