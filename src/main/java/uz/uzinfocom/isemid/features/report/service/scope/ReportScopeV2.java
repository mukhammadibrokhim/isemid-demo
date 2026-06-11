package uz.uzinfocom.isemid.features.report.service.scope;

public record ReportScopeV2(
    String specification,
    String parentCode,
    Long currentOrganizationId,
    int hierarchyLevelNumber

) {}
