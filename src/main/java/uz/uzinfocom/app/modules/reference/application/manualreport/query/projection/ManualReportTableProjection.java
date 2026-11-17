package uz.uzinfocom.app.modules.reference.application.manualreport.query.projection;

public interface ManualReportTableProjection {

    Long getId();

    String getCode();

    String getShortName();

    String getNameUz();

    String getNameUzCyril();

    String getNameRu();

    String getNameKaa();

    Boolean getIncludeInTotal();

    Boolean getDeleted();
}
