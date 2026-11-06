package uz.uzinfocom.app.integration.outbound.monitoring.application;

/**
 * One (period, location, disease block) group. {@code diseaseCode} is the
 * resolved ICD-10 block (or the case's own code when it has no block
 * ancestor) and is {@code null} for cases with no usable final diagnosis -
 * and for periods with no cases at all, where every count is 0 and
 * {@code locationCode} is the filtered location (or {@code null}).
 */
public record MonitoringDynamicsRow(
        String period,
        String locationCode,
        String diseaseCode,
        long confirmed,
        long unconfirmed,
        long male,
        long female,
        long age0To3,
        long age4To6,
        long age7To10,
        long age11To17,
        long age18To25,
        long age26To45,
        long age46To65,
        long age66Plus
) {
}
