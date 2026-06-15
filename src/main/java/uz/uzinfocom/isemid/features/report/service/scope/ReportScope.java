package uz.uzinfocom.isemid.features.report.service.scope;

public record ReportScope(
    String specification,
    String parentCode,
    Long currentOrganizationId,
    int hierarchyLevelNumber,
    boolean hasPermission

) {}
