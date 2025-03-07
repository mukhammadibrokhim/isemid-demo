package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.configs.jwt.JwtUtil;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.dtos.TokenDto;
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
    public TokenDto generateToken() {
        User user = userRepository.findByNnuzb("31611892930045").orElse(null);
        String token = jwtUtil.generateToken(user);
        return TokenDto.builder()
                .token_type("Bearer")
                .expires_in(JwtUtil.EXPIRATION_TIME)
                .access_token(token)
                .refresh_token(null) // Agar refresh token bo‘lsa, shu yerda qo‘shasiz
                .build();
    }
}