package uz.uzinfocom.isemid.features.form129.util;

import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.patient.models.Patient;

public class Form129BindingUtil {
    public static void bindChildEntity(Form129 form129) {
        Patient patient = form129.getPatient();
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
