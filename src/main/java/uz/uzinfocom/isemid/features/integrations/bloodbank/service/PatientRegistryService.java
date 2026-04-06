package uz.uzinfocom.isemid.features.integrations.bloodbank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientRegistryService {

    private final Form058Repository form058Repository;

    public List<InfectedPatientResponse> getInfectedPatientResponses(String nnuzb) {
        return form058Repository.getInfectedPatientResponses(nnuzb);
    }
}
