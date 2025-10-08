package uz.uzinfocom.isemid.features.form058.util;

import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Patient;

public class Form058BindingUtil {
    public static void bindChildEntity(Form058 form058) {
        Patient patient = form058.getPatient();
        if (patient != null) {
            if (patient.getIdentifiers() != null) {
                patient.getIdentifiers().forEach(i -> i.setPatient(patient));
            }
            if (patient.getAddresses() != null) {
                patient.getAddresses().forEach(a -> a.setPatient(patient));
            }
            if (patient.getAffiliations() != null) {
                patient.getAffiliations().forEach(aff -> aff.setPatient(patient));
            }
        }
    }
}
