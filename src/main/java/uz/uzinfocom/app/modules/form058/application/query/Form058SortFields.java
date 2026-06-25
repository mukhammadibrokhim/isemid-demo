package uz.uzinfocom.app.modules.form058.application.query;

import java.util.Map;

public final class Form058SortFields {

    public static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("mkb10Code", "diagnosisInfo.mkb10Code"),
            Map.entry("mkb10Name", "diagnosisInfo.mkb10Name"),
            Map.entry("visitDate", "dateInfo.visitDate"),
            Map.entry("initialReportDateTime", "dateInfo.initialReportDateTime"),
            Map.entry("senderOrganizationId", "senderOrganizationId"),
            Map.entry("receiverOrganizationId", "receiverOrganizationId"),
            Map.entry("hasLinkedCards", "hasLinkedCards"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private Form058SortFields() {
    }
}