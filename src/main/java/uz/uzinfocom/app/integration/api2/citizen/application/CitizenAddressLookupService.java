package uz.uzinfocom.app.integration.api2.citizen.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.api2.citizen.application.mapper.CitizenAddressMapper;
import uz.uzinfocom.app.integration.api2.citizen.client.CitizenApi2Client;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.platform.observability.TraceContext;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Enriches a /citizen NNUZB lookup with address data from v3/citizenAddress. Kept out of {@link
 * CitizenLookupService} deliberately: that service is type-agnostic (NNUZB/PPN/CZ) and address
 * lookup only ever applies to the NNUZB path (it needs the same nnuzb+birth_date the caller
 * already supplied), and unlike the primary lookup this one is best-effort - a slow or failing
 * upstream must degrade to an empty address list, never fail the /citizen response.
 */
@Service
public class CitizenAddressLookupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CitizenAddressLookupService.class);

    private final CitizenApi2Client citizenApi2Client;
    private final CitizenAddressMapper addressMapper;
    private final Api2Properties properties;
    private final Executor executor;

    public CitizenAddressLookupService(
            CitizenApi2Client citizenApi2Client,
            CitizenAddressMapper addressMapper,
            Api2Properties properties,
            @Qualifier("applicationTaskExecutor") Executor executor
    ) {
        this.citizenApi2Client = citizenApi2Client;
        this.addressMapper = addressMapper;
        this.properties = properties;
        this.executor = executor;
    }

    /**
     * Never throws. Waits at most {@code integration.api2.citizen-address-enrichment-timeout}
     * (default 3s) for citizenAddress to answer; on timeout, upstream error, or missing input it
     * returns an empty list so the caller's /citizen response still comes back with whatever it
     * already has.
     */
    public List<CitizenAddressResponse> lookupWithTimeout(String nnuzb, LocalDate birthDate) {
        if (!StringUtils.hasText(nnuzb) || birthDate == null) {
            return List.of();
        }

        // Api2BearerTokenInterceptor pulls the caller's token off SecurityContextHolder, and
        // CitizenAddressMapper's region/district/neighborhood name resolution pulls the request
        // locale off LocaleContextHolder - both are thread-local, so the executor's pooled worker
        // thread has neither unless they're captured here (on the request thread, where they're
        // actually populated) and re-applied inside the task. The pool is shared with unrelated
        // async work (audit, notifications, webhooks), so both are explicitly cleared in a finally
        // block - otherwise the caller's identity/locale would leak into whatever that thread runs
        // next.
        SecurityContext callerSecurityContext = SecurityContextHolder.getContext();
        LocaleContext callerLocaleContext = LocaleContextHolder.getLocaleContext();

        return CompletableFuture
                .supplyAsync(() -> fetchAs(callerSecurityContext, callerLocaleContext, nnuzb, birthDate), executor)
                .orTimeout(properties.citizenAddressEnrichmentTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(this::onFailure)
                .join();
    }

    private List<CitizenAddressResponse> fetchAs(
            SecurityContext callerSecurityContext,
            LocaleContext callerLocaleContext,
            String nnuzb,
            LocalDate birthDate
    ) {
        SecurityContextHolder.setContext(callerSecurityContext);
        LocaleContextHolder.setLocaleContext(callerLocaleContext);
        try {
            return fetch(nnuzb, birthDate);
        } finally {
            LocaleContextHolder.resetLocaleContext();
            SecurityContextHolder.clearContext();
        }
    }

    private List<CitizenAddressResponse> fetch(String nnuzb, LocalDate birthDate) {
        CitizenAddressLookupResult result = citizenApi2Client.lookupAddress(nnuzb, birthDate);
        return addressMapper.map(result.data());
    }

    private List<CitizenAddressResponse> onFailure(Throwable throwable) {
        Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
        LOGGER.warn(
                "event=citizen_address_lookup_unavailable traceId={} reason={} message={}",
                TraceContext.currentTraceId(),
                cause.getClass().getSimpleName(),
                cause.getMessage()
        );
        return List.of();
    }
}
