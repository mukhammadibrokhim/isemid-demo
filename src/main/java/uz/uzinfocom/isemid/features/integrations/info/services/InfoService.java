package uz.uzinfocom.isemid.features.integrations.info.services;

import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.List;

public interface InfoService {
    List<Form058> getPatientMHByNnuzb(String nnuzb);

    List<Form058> getConfirmedMkb10ByNnuzb(String nnuzb);
}
