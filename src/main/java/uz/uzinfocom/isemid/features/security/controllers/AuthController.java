package uz.uzinfocom.isemid.features.security.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.configs.security.jwt.JwtUtil;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.security.dtos.requests.AuthRequest;
import uz.uzinfocom.isemid.features.security.dtos.requests.RegisterRequest;
import uz.uzinfocom.isemid.features.security.dtos.responses.TokenResponse;
import uz.uzinfocom.isemid.features.security.services.AuthService;
import uz.uzinfocom.isemid.features.user.models.User;

@RestController
@RequestMapping(ApiPaths.Auth.ROOT)
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final MessageUtil messageUtil;

    @Operation(summary = "Аутентификация пользователя",
            description = "Принимает nnuzb и пароль, возвращает JWT токен при успешной аутентификации.")
    @PostMapping(value = "/sign-in")
    public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getNnuzb(), authRequest.getPassword()));
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(TokenResponse.builder()
                .token_type("Bearer")
                .expires_in(JwtUtil.EXPIRATION_TIME)
                .access_token(token)
                .refresh_token(null)
                .build());
    }

    @Operation(summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя с переданными данными регистрации.")
    @PostMapping(value = "/sign-up")
    public ApiResponse<?> signUp(@Valid @RequestBody RegisterRequest request) {
        authService.signUp(request);
        return ApiResponse.success(messageUtil, null);
    }
}