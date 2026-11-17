package uz.uzinfocom.app.platform.http.ssrf;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * SSRF address policy for outbound calls whose target URL is configured at
 * runtime (today: the per-client outbound webhook). Decides on the
 * <em>resolved</em> IP, not the host string, so a DNS name pointing at an
 * internal address ({@code 127.0.0.1.nip.io}), a numeric IPv4 form
 * ({@code 2130706433}) or an IPv4-mapped IPv6 literal can't slip past it.
 *
 * <p>Always forbidden: loopback, unspecified ({@code 0.0.0.0}/{@code ::}),
 * link-local (incl. the {@code 169.254.169.254} cloud-metadata endpoint),
 * multicast, broadcast and the reserved {@code 0/8}, {@code 240/4} blocks.
 * Private networks (RFC 1918, CGNAT {@code 100.64/10}, IPv6 ULA
 * {@code fc00::/7}, deprecated site-local {@code fec0::/10}) are forbidden
 * unless the caller explicitly allows them.
 */
public final class OutboundAddressPolicy {

    private OutboundAddressPolicy() {
    }

    public static boolean isForbidden(InetAddress address, boolean allowPrivateNetwork) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        if (address instanceof Inet4Address) {
            return isForbiddenIpv4(address.getAddress(), allowPrivateNetwork);
        }
        if (address instanceof Inet6Address) {
            return isForbiddenIpv6(address.getAddress(), allowPrivateNetwork);
        }
        return true;
    }

    /**
     * Save-time check for a configured URL host. A literal or resolvable host
     * is rejected if any of its addresses is forbidden; a host that doesn't
     * resolve right now is let through - the connection-time
     * {@link GuardedDnsResolver} re-checks every address on every request,
     * which is the check that actually matters (DNS can change after saving).
     */
    public static boolean isForbiddenHost(String host, boolean allowPrivateNetwork) {
        if (host == null || host.isBlank()) {
            return true;
        }

        String normalized = host.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (normalized.equals("localhost") || normalized.endsWith(".localhost")) {
            return true;
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(normalized);
        } catch (UnknownHostException unresolvable) {
            return false;
        }

        for (InetAddress address : addresses) {
            if (isForbidden(address, allowPrivateNetwork)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isForbiddenIpv4(byte[] octets, boolean allowPrivateNetwork) {
        int first = octets[0] & 0xff;
        int second = octets[1] & 0xff;

        // 0.0.0.0/8 "this network", 240.0.0.0/4 reserved + 255.255.255.255 broadcast.
        if (first == 0 || first >= 240) {
            return true;
        }

        boolean privateNetwork = first == 10
                || (first == 172 && second >= 16 && second <= 31)
                || (first == 192 && second == 168)
                || (first == 100 && second >= 64 && second <= 127);
        return privateNetwork && !allowPrivateNetwork;
    }

    private static boolean isForbiddenIpv6(byte[] bytes, boolean allowPrivateNetwork) {
        // IPv4-compatible (::a.b.c.d) and NAT64 (64:ff9b::/96) embed an IPv4 target -
        // judge them by that embedded address. (IPv4-mapped ::ffff:a.b.c.d is already
        // turned into an Inet4Address by the JDK.)
        if (isZero(bytes, 0, 12) || isNat64Prefix(bytes)) {
            byte[] embedded = {bytes[12], bytes[13], bytes[14], bytes[15]};
            try {
                return isForbidden(InetAddress.getByAddress(embedded), allowPrivateNetwork);
            } catch (UnknownHostException impossible) {
                return true;
            }
        }

        int first = bytes[0] & 0xff;
        int second = bytes[1] & 0xff;
        boolean uniqueLocal = (first & 0xfe) == 0xfc;
        boolean siteLocal = first == 0xfe && (second & 0xc0) == 0xc0;
        return (uniqueLocal || siteLocal) && !allowPrivateNetwork;
    }

    private static boolean isNat64Prefix(byte[] bytes) {
        return (bytes[0] & 0xff) == 0x00 && (bytes[1] & 0xff) == 0x64
                && (bytes[2] & 0xff) == 0xff && (bytes[3] & 0xff) == 0x9b
                && isZero(bytes, 4, 12);
    }

    private static boolean isZero(byte[] bytes, int fromInclusive, int toExclusive) {
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
