package uz.uzinfocom.isemid.features.report.projection;

public record MonitoringLocationRow(
        String regionCode, long totalCases, long confirmed, long unconfirmed, long male, long female, long age0To3,
        long age4To6, long age7To10, long age11To17, long age18To25, long age26To45, long age46To65, long age66Plus) {
}