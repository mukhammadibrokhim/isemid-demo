package uz.uzinfocom.isemid.commons.configs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uz.uzinfocom.isemid.commons.configs.security.filter.RevokedTokenFilter;
import uz.uzinfocom.isemid.commons.configs.security.jwt.JwtAuthConverter;
import uz.uzinfocom.isemid.commons.configs.security.jwt.JwtAuthHelper;
import uz.uzinfocom.isemid.features.user.services.impl.UserServiceImpl;

import static uz.uzinfocom.isemid.commons.configs.security.SecurityConstants.WHITE_LIST;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthHelper jwtAuthHelper;
    private final AccessDeniedEntryPoint accessDeniedEntryPoint;
    private final AuthEntryPoint authEntryPoint;
    private final RevokedTokenFilter revokedTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authRequests -> authRequests
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers("/v1/db/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedEntryPoint)
                )
                .addFilterBefore(revokedTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                        .jwtAuthenticationConverter(new JwtAuthConverter(jwtAuthHelper)))
                                .authenticationEntryPoint(authEntryPoint)
                                .accessDeniedHandler(accessDeniedEntryPoint)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserServiceImpl userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider::authenticate;
    }
}
