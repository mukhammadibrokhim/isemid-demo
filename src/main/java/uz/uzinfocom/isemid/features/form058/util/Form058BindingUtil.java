package uz.uzinfocom.isemid.features.form058.util;

import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Patient;

public class Form058BindingUtil {
    public static void bindChildEntity(Form058 form058) {
        Patient patient = form058.getPatient();
        if (patient != null && patient.getIdentifiers() != null) {
            patient.getIdentifiers().forEach(identifier -> identifier.setPatient(patient));
            patient.getAddresses().forEach(address -> address.setPatient(patient));
        }
        if ((patient != null ? patient.getAffiliations() : null) != null) {
            patient.getAffiliations().forEach(affiliation -> affiliation.setPatient(patient));
        }
    }
}
