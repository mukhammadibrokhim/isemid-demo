package uz.uzinfocom.app.platform.http.ssrf;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Connection-time half of the SSRF guard: the Apache HttpClient resolves the
 * target host through this resolver right before opening the socket, so the
 * {@link OutboundAddressPolicy} check runs on the exact addresses being
 * connected to. Checking only when the URL is saved would miss DNS records
 * that change afterwards (DNS rebinding). Any forbidden address fails the
 * whole lookup, which the client surfaces as an ordinary connect failure.
 */
public class GuardedDnsResolver implements DnsResolver {

    private final DnsResolver delegate;
    private final boolean allowPrivateNetwork;

    public GuardedDnsResolver(boolean allowPrivateNetwork) {
        this(SystemDefaultDnsResolver.INSTANCE, allowPrivateNetwork);
    }

    GuardedDnsResolver(DnsResolver delegate, boolean allowPrivateNetwork) {
        this.delegate = delegate;
        this.allowPrivateNetwork = allowPrivateNetwork;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = delegate.resolve(host);
        for (InetAddress address : addresses) {
            if (OutboundAddressPolicy.isForbidden(address, allowPrivateNetwork)) {
                throw new UnknownHostException("Outbound target host resolves to a forbidden address: " + host);
            }
        }
        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        return delegate.resolveCanonicalHostname(host);
    }
}
