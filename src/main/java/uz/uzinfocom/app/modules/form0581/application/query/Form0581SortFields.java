package uz.uzinfocom.app.modules.form0581.application.query;

import java.util.Map;

public final class Form0581SortFields {

    public static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("mkb10Code", "diagnosisInfo.mkb10Code"),
            Map.entry("mkb10Name", "diagnosisInfo.mkb10Name"),
            Map.entry("injuryDateTime", "incidentInfo.injuryDateTime"),
            Map.entry("dpuVisitDateTime", "incidentInfo.dpuVisitDateTime"),
            Map.entry("senderOrganizationId", "senderOrganizationId"),
            Map.entry("receiverOrganizationId", "receiverOrganizationId"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private Form0581SortFields() {
    }
}
