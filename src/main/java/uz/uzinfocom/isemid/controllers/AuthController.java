package uz.uzinfocom.isemid.controllers;

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
import uz.uzinfocom.isemid.configs.jwt.JwtUtil;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.dtos.TokenDto;
import uz.uzinfocom.isemid.models.payload.requests.ReqAuth;
import uz.uzinfocom.isemid.models.payload.requests.ReqRegister;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.services.UserService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.AUTH;


@RestController
@RequestMapping(AUTH)
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    final AuthenticationManager authenticationManager;
    final JwtUtil jwtUtil;
    final UserRepository userRepository;
    final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<TokenDto> signIn(@Valid @RequestBody final ReqAuth reqAuth) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(reqAuth.getNnuzb(), reqAuth.getPassword()));
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new TokenDto("Bearer", JwtUtil.EXPIRATION_TIME, token, null));
    }

    @PostMapping("/sign-up")
    public ApiResponse<?> signUp(@Valid @RequestBody final ReqRegister reqRegister) {
        return userService.signUpUser(reqRegister);
    }
}