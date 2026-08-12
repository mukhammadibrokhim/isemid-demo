package uz.uzinfocom.app.integration.api2.citizen.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.citizen.application.mapper.CitizenAddressMapper;
import uz.uzinfocom.app.integration.api2.citizen.client.CitizenApi2Client;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.integration.api2.common.exception.Api2UnavailableException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenAddressLookupServiceTest {

    private final CitizenApi2Client citizenApi2Client = mock(CitizenApi2Client.class);
    private final CitizenAddressMapper addressMapper =
            new CitizenAddressMapper(mock(ReferenceLookupService.class));
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Executor executor = Executors.newCachedThreadPool();

    @AfterEach
    void clearThreadLocalContext() {
        SecurityContextHolder.clearContext();
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void propagatesCallerSecurityContextIntoTheAsyncTaskAndClearsItAfterward() throws Exception {
        Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
        Authentication authentication = new TestingAuthenticationToken("someone", "n/a");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AtomicReference<Authentication> seenInsideTask = new AtomicReference<>();
        when(citizenApi2Client.lookupAddress("12345678901234", LocalDate.of(2020, 1, 1)))
                .thenAnswer(invocation -> {
                    seenInsideTask.set(SecurityContextHolder.getContext().getAuthentication());
                    return new CitizenAddressLookupResult(200, jsonMapper.readTree("{}"));
                });

        CitizenAddressLookupService service = new CitizenAddressLookupService(
                citizenApi2Client, addressMapper, properties(Duration.ofSeconds(3)), singleThreadExecutor);

        service.lookupWithTimeout("12345678901234", LocalDate.of(2020, 1, 1));

        assertThat(seenInsideTask.get()).isEqualTo(authentication);

        // The pool is shared with unrelated work - the context must not still be
        // sitting on that thread for whatever runs on it next.
        Authentication leakedIntoNextTask = CompletableFuture
                .supplyAsync(() -> SecurityContextHolder.getContext().getAuthentication(), singleThreadExecutor)
                .get();
        assertThat(leakedIntoNextTask).isNull();
    }

    @Test
    void propagatesCallerLocaleIntoTheAsyncTaskAndClearsItAfterward() throws Exception {
        Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
        LocaleContextHolder.setLocale(Locale.of("ru"));

        AtomicReference<Locale> seenInsideTask = new AtomicReference<>();
        when(citizenApi2Client.lookupAddress("12345678901234", LocalDate.of(2020, 1, 1)))
                .thenAnswer(invocation -> {
                    seenInsideTask.set(LocaleContextHolder.getLocale());
                    return new CitizenAddressLookupResult(200, jsonMapper.readTree("{}"));
                });

        CitizenAddressLookupService service = new CitizenAddressLookupService(
                citizenApi2Client, addressMapper, properties(Duration.ofSeconds(3)), singleThreadExecutor);

        service.lookupWithTimeout("12345678901234", LocalDate.of(2020, 1, 1));

        assertThat(seenInsideTask.get()).isEqualTo(Locale.of("ru"));

        // The pool is shared with unrelated work - the locale must not still be
        // sitting on that thread for whatever runs on it next.
        Locale leakedIntoNextTask = CompletableFuture
                .supplyAsync(LocaleContextHolder::getLocale, singleThreadExecutor)
                .get();
        assertThat(leakedIntoNextTask).isNotEqualTo(Locale.of("ru"));
    }

    @Test
    void missingNnuzbOrBirthDateSkipsTheUpstreamCallEntirely() {
        CitizenAddressLookupService service = service(Duration.ofSeconds(3));

        assertThat(service.lookupWithTimeout(null, LocalDate.of(2020, 1, 1))).isEmpty();
        assertThat(service.lookupWithTimeout("12345678901234", null)).isEmpty();
    }

    @Test
    void upstreamFailureDegradesToEmptyListInsteadOfPropagating() {
        when(citizenApi2Client.lookupAddress("12345678901234", LocalDate.of(2020, 1, 1)))
                .thenThrow(new Api2UnavailableException("CITIZEN_ADDRESS_LOOKUP", new RuntimeException("boom")));
        CitizenAddressLookupService service = service(Duration.ofSeconds(3));

        List<CitizenAddressResponse> addresses =
                service.lookupWithTimeout("12345678901234", LocalDate.of(2020, 1, 1));

        assertThat(addresses).isEmpty();
    }

    @Test
    void slowUpstreamBeyondTheBudgetDegradesToEmptyListRatherThanBlocking() throws InterruptedException {
        CountDownLatch releaseUpstream = new CountDownLatch(1);
        when(citizenApi2Client.lookupAddress("12345678901234", LocalDate.of(2020, 1, 1)))
                .thenAnswer(invocation -> {
                    releaseUpstream.await();
                    return new CitizenAddressLookupResult(200, jsonMapper.readTree("{}"));
                });
        CitizenAddressLookupService service = service(Duration.ofMillis(200));

        long start = System.nanoTime();
        List<CitizenAddressResponse> addresses =
                service.lookupWithTimeout("12345678901234", LocalDate.of(2020, 1, 1));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(addresses).isEmpty();
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));

        releaseUpstream.countDown();
    }

    private CitizenAddressLookupService service(Duration enrichmentTimeout) {
        return new CitizenAddressLookupService(
                citizenApi2Client, addressMapper, properties(enrichmentTimeout), executor);
    }

    private Api2Properties properties(Duration enrichmentTimeout) {
        return new Api2Properties(
                "https://api2.example",
                Duration.ofSeconds(3),
                Duration.ofSeconds(15),
                new Api2Properties.Endpoints(
                        "/v3/Child", "/v3/Citizen", "/v3/CitizenPassport", "/v3/citizenAddress", "/v3/LegalEntity"
                ),
                enrichmentTimeout
        );
    }
}
