package uz.uzinfocom.isemid.features.integrations.gcp.service;

import org.springframework.http.ResponseEntity;

public interface CitizenService {

    Object getCitizenByNnuzb(String nnuzb, String photo, String token);

    Object getCitizenByChild(String series, String number, String token);

    ResponseEntity<?> getCitizenByNnuzbByToken(String nnuzb);
}
