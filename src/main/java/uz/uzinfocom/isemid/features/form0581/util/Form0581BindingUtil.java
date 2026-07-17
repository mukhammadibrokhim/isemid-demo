package uz.uzinfocom.isemid.features.form0581.util;

import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.patient.models.Patient;

public class Form0581BindingUtil {
    public static void bindChildEntity(Form0581 form0581) {
        Patient patient = form0581.getPatient();
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

        if (form0581.getBittenPersons() != null) {
            form0581.getBittenPersons().forEach(bp -> bp.setForm0581(form0581));
        }
    }
}
