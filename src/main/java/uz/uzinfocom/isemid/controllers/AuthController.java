package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.configs.jwt.JwtUtil;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.UserRepository;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.AUTH;


@RestController
@RequestMapping(AUTH)
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    final AuthenticationManager authenticationManager;
    final JwtUtil jwtUtil;
    final UserRepository userRepository;

    @GetMapping
    public ApiResponse<?> generateToken() {
        User user = userRepository.findByNnuzb("31611892930045").orElse(null);
        return new ApiResponse<>(true, "", jwtUtil.generateToken(user));
    }
}