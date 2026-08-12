package uz.uzinfocom.isemid.features.integrations.lis.dto;


import java.time.Instant;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LisResponse<T> {
    private final T data;
    private final Meta meta;

    private LisResponse(T data, Meta meta) {
        this.data = data;
        this.meta = meta;
    }

    public static <T> LisResponse<T> of(T data) {
        return new LisResponse<>(data, Meta.of());
    }

    private record Meta(Instant timestamp) {
        public static Meta of() {
            return new Meta(Instant.now());
        }
    }
}
