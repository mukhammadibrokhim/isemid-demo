package uz.uzinfocom.app.integration.lis.client.callback;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads the raw JSON body LIS posts to
 * {@code POST /v1/acts/{id}/lis/callback} and decides whether it carries a
 * final laboratory result ({@link Outcome#COMPLETED}) or LIS sent the act
 * back for rework ({@link Outcome#RETURNED}).
 *
 * <p><b>TODO(LIS-spec):</b> LIS's exact callback contract for a returned act
 * is not confirmed yet — there is no {@code Act.xlsx}-equivalent field list.
 * Until it is, this uses a deliberately broad heuristic: any recognizable
 * "status"-like key whose text value looks like a rejection/return, or any
 * truthy {@code rejected}/{@code returned}-style flag, is treated as a
 * return. Everything else stays {@code COMPLETED}, so the historical
 * behaviour (every callback concludes the act) is the default and nothing
 * regresses if the heuristic misses.
 */
public final class LisCallbackInterpreter {

    public enum Outcome {
        COMPLETED,
        RETURNED
    }

    public record Result(Outcome outcome, String reason) {
    }

    /** Keys whose string value is inspected for a return/rejection marker. */
    private static final Set<String> STATUS_KEYS =
            Set.of("status", "state", "actstatus", "act_status", "result", "resultstatus", "result_status", "decision");

    /** Substrings (lower-cased) in a status value that mean "sent back". */
    private static final List<String> RETURN_MARKERS =
            List.of("return", "reject", "declin", "send_back", "sendback", "sent_back", "sentback", "revis", "rework", "cancel");

    /** Boolean-ish keys that, when truthy, mean the act was sent back. */
    private static final Set<String> RETURN_FLAG_KEYS =
            Set.of("rejected", "returned", "declined", "sentback", "sent_back", "isreturned", "isrejected");

    /** Keys carrying a human-readable reason, in preference order. */
    private static final List<String> REASON_KEYS =
            List.of("rejectreason", "reject_reason", "returnreason", "return_reason", "reason", "message", "comment", "note");

    private static final String DEFAULT_RETURN_REASON = "LIS_RETURNED";

    private LisCallbackInterpreter() {
    }

    public static Result interpret(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return new Result(Outcome.COMPLETED, null);
        }

        Map<String, Object> normalized = normalizeKeys(body);

        if (looksReturned(normalized)) {
            return new Result(Outcome.RETURNED, extractReason(normalized));
        }
        return new Result(Outcome.COMPLETED, null);
    }

    private static boolean looksReturned(Map<String, Object> body) {
        for (String key : RETURN_FLAG_KEYS) {
            if (isTruthy(body.get(key))) {
                return true;
            }
        }
        for (String key : STATUS_KEYS) {
            Object value = body.get(key);
            if (value != null && containsReturnMarker(value.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsReturnMarker(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return RETURN_MARKERS.stream().anyMatch(lower::contains);
    }

    private static boolean isTruthy(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.doubleValue() != 0d;
        }
        if (value instanceof String s) {
            return s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("yes");
        }
        return false;
    }

    private static String extractReason(Map<String, Object> body) {
        for (String key : REASON_KEYS) {
            Object value = body.get(key);
            if (value instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return DEFAULT_RETURN_REASON;
    }

    private static Map<String, Object> normalizeKeys(Map<String, Object> body) {
        return body.entrySet().stream().collect(
                java.util.HashMap::new,
                (map, entry) -> {
                    if (entry.getKey() != null) {
                        map.putIfAbsent(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
                    }
                },
                java.util.HashMap::putAll
        );
    }
}
