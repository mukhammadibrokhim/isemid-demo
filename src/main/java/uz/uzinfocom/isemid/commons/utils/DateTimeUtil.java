package uz.uzinfocom.isemid.commons.utils;

import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtil {

    // Constant time zone: Asia/Tashkent
    private static final ZoneId ZONE_TASHKENT = ZoneId.of("Asia/Tashkent");

    // Formatter for date only (dd-MM-yyyy)
    // Using withZone() so we don't need to call atZone() every time
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZONE_TASHKENT);

    // Formatter for date and time (dd-MM-yyyy HH:mm)
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZONE_TASHKENT);

    /**
     * Formats a LocalDateTime into a date-time string in Asia/Tashkent zone
     *
     * @param dateTime LocalDateTime object
     * @return formatted date-time string or empty string if null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatInstant(
                dateTime == null ? null : dateTime.atZone(ZONE_TASHKENT).toInstant(),
                DATE_TIME_FORMATTER
        );
    }

    /**
     * Formats a Timestamp into a date-time string in Asia/Tashkent zone
     *
     * @param timestamp SQL Timestamp object
     * @return formatted date-time string or empty string if null
     */
    public static String formatDateTime(Timestamp timestamp) {
        return formatInstant(
                timestamp == null ? null : timestamp.toInstant(),
                DATE_TIME_FORMATTER
        );
    }

    /**
     * Formats a LocalDateTime into a date string in Asia/Tashkent zone
     *
     * @param dateTime LocalDateTime object
     * @return formatted date string or empty string if null
     */
    public static String formatDate(LocalDateTime dateTime) {
        return formatInstant(
                dateTime == null ? null : dateTime.atZone(ZONE_TASHKENT).toInstant(),
                DATE_FORMATTER
        );
    }

    /**
     * Generic formatter for Instant values
     * Returns empty string if null
     *
     * @param instant   Instant object (UTC point in time)
     * @param formatter DateTimeFormatter (pre-bound with a time zone)
     * @return formatted string
     */
    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        return instant != null ? formatter.format(instant) : "";
    }
}
