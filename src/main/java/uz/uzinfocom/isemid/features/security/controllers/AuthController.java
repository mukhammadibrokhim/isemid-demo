package uz.uzinfocom.isemid.features.security.controllers;

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
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.configs.security.jwt.JwtUtil;
import uz.uzinfocom.isemid.features.security.dtos.requests.AuthRequest;
import uz.uzinfocom.isemid.features.security.dtos.requests.RegisterRequest;
import uz.uzinfocom.isemid.features.security.dtos.responses.TokenResponse;
import uz.uzinfocom.isemid.features.security.services.AuthService;
import uz.uzinfocom.isemid.features.user.models.User;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(ApiPaths.Auth.ROOT)
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    final AuthenticationManager authenticationManager;
    final JwtUtil jwtUtil;
    final AuthService authService;

    @PostMapping(value = "/sign-in", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/sign-up", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ApiResponse<?> signUp(@Valid @RequestBody RegisterRequest request) {
        return authService.signUp(request);
    }
}