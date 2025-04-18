package uz.uzinfocom.isemid.features.security.services;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.security.dtos.requests.RegisterRequest;

public interface AuthService {
    ApiResponse<?> signUp(RegisterRequest request);
}
