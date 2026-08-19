package uz.uzinfocom.app.modules.reference.application.lookup.projection;

public interface GeoReferenceItemProjection extends ReferenceItemProjection {

    Integer getSoatoId();

    String getTin();

    String getUzcadRegistryCode();
}
