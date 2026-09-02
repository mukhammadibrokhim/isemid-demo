package uz.uzinfocom.app.integration.lis.client.callback;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.lis.client.callback.LisCallbackInterpreter.Outcome;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LisCallbackInterpreterTest {

    @Test
    void nullOrEmptyBodyIsCompleted() {
        assertThat(LisCallbackInterpreter.interpret(null).outcome()).isEqualTo(Outcome.COMPLETED);
        assertThat(LisCallbackInterpreter.interpret(Map.of()).outcome()).isEqualTo(Outcome.COMPLETED);
    }

    @Test
    void plainResultBodyIsCompleted() {
        Map<String, Object> body = Map.of("id", 12, "result", "ok", "parameters", "...");
        assertThat(LisCallbackInterpreter.interpret(body).outcome()).isEqualTo(Outcome.COMPLETED);
    }

    @Test
    void statusValueContainingReturnMarkerIsReturned() {
        assertThat(LisCallbackInterpreter.interpret(Map.of("status", "RETURNED")).outcome())
                .isEqualTo(Outcome.RETURNED);
        assertThat(LisCallbackInterpreter.interpret(Map.of("STATE", "sent_back")).outcome())
                .isEqualTo(Outcome.RETURNED);
        assertThat(LisCallbackInterpreter.interpret(Map.of("decision", "REJECT")).outcome())
                .isEqualTo(Outcome.RETURNED);
    }

    @Test
    void truthyReturnFlagIsReturned() {
        assertThat(LisCallbackInterpreter.interpret(Map.of("rejected", true)).outcome())
                .isEqualTo(Outcome.RETURNED);
        assertThat(LisCallbackInterpreter.interpret(Map.of("isReturned", "true")).outcome())
                .isEqualTo(Outcome.RETURNED);
    }

    @Test
    void reasonIsExtractedFromKnownKeysElseDefault() {
        Map<String, Object> withReason = new HashMap<>();
        withReason.put("status", "RETURNED");
        withReason.put("rejectReason", "namuna buzilgan");
        assertThat(LisCallbackInterpreter.interpret(withReason).reason()).isEqualTo("namuna buzilgan");

        assertThat(LisCallbackInterpreter.interpret(Map.of("returned", true)).reason())
                .isEqualTo("LIS_RETURNED");
    }
}
