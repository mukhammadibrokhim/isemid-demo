package uz.uzinfocom.app.platform.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtTokenValidatorFactory {

    public OAuth2TokenValidator<Jwt> create(
            String providerKey,
            AuthProvidersProperties.ProviderProperties provider
    ) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        validators.add(JwtValidators.createDefault());

        if (provider.isValidateIssuer()) {
            if (!StringUtils.hasText(provider.getIssuerUri())) {
                throw new IllegalStateException(
                        "Issuer URI is required when validateIssuer=true. providerKey=" + providerKey
                );
            }

            validators.add(new JwtIssuerValidator(provider.getIssuerUri()));
        }

        if (provider.isValidateAudience()) {
            // AudienceValidator accepts every token when its list is empty, so
            // validateAudience=true with no audiences would silently check nothing.
            if (provider.getAudiences() == null || provider.getAudiences().stream().noneMatch(StringUtils::hasText)) {
                throw new IllegalStateException(
                        "Audiences are required when validateAudience=true. providerKey=" + providerKey
                );
            }

            validators.add(new AudienceValidator(provider.getAudiences()));
        } else {
            log.warn(
                    "JWT audience validation is disabled - tokens issued by this provider for any client are accepted. providerKey={}",
                    providerKey
            );
        }

        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}