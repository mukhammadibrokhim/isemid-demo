package uz.uzinfocom.app.platform.reference.application.icd10.query.projection;

public interface Icd10ChildrenCountProjection {

    Long getParentId();

    Long getChildrenCount();
}
