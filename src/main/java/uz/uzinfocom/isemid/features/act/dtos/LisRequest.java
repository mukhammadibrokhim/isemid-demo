package uz.uzinfocom.isemid.features.act.dtos;

import lombok.Builder;

@Builder
public record LisRequest(
    Long actId,
    Long lisLabId,
    Priority priority,
    Boolean paid
) {
}
