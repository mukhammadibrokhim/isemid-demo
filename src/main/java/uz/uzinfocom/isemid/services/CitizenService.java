package uz.uzinfocom.isemid.services;

public interface CitizenService {

    Object getCitizenByNnuzb(String nnuzb, String photo, String token);

    Object getCitizenByChild(String series, String number, String token);
}
