package uz.uzinfocom.isemid.features.forecast.models.projection;

import java.time.LocalDate;

public interface DayCountProjection {
    LocalDate getDay();

    long getCnt();
}
