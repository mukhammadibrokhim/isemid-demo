package uz.uzinfocom.isemid.features.act.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LisRequest(
    Long actId,
    Long lisLabId,
    Priority priority,
    Boolean paid,
    @JsonProperty("force")
    Boolean isAllowedDuplicate
) {
}
