package uz.uzinfocom.isemid.features.integrations.gcp.service;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface CitizenService {

    Object getCitizenByNnuzb(String nnuzb, LocalDate birthDate, String photo, String token);

    Object getCitizenByChild(String series, String number, String token);

    ResponseEntity<?> getCitizenByNnuzbByToken(String nnuzb, LocalDate birthDate);
}
