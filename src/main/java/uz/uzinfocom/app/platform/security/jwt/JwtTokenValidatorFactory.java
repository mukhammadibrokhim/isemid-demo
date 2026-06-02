package uz.uzinfocom.app.platform.security.jwt;

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
            validators.add(new AudienceValidator(provider.getAudiences()));
        }

        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}