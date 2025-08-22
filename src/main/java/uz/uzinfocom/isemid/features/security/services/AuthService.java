package uz.uzinfocom.isemid.features.security.services;

import uz.uzinfocom.isemid.features.security.dtos.requests.RegisterRequest;

public interface AuthService {
    void signUp(RegisterRequest request);
}
