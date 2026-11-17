package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class VerifiedSecretCachingPasswordEncoderTest {

    private final PasswordEncoder bcrypt = spy(new BCryptPasswordEncoder(4));
    private final PasswordEncoder encoder = new VerifiedSecretCachingPasswordEncoder(bcrypt, Duration.ofMinutes(10), 100);

    @Test
    void runsBcryptOnlyOnceForARepeatedCorrectSecret() {
        String hash = bcrypt.encode("s3cret");

        assertThat(encoder.matches("s3cret", hash)).isTrue();
        assertThat(encoder.matches("s3cret", hash)).isTrue();
        assertThat(encoder.matches("s3cret", hash)).isTrue();

        verify(bcrypt, times(1)).matches("s3cret", hash);
    }

    @Test
    void neverCachesAWrongSecret() {
        String hash = bcrypt.encode("s3cret");

        assertThat(encoder.matches("wrong", hash)).isFalse();
        assertThat(encoder.matches("wrong", hash)).isFalse();

        verify(bcrypt, times(2)).matches("wrong", hash);
    }

    @Test
    void oldSecretStopsMatchingOnceTheStoredHashIsRotated() {
        String oldHash = bcrypt.encode("old");
        assertThat(encoder.matches("old", oldHash)).isTrue();

        String rotatedHash = bcrypt.encode("new");

        assertThat(encoder.matches("old", rotatedHash)).isFalse();
        assertThat(encoder.matches("new", rotatedHash)).isTrue();
    }
}
