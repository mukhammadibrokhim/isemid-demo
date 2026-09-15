package uz.uzinfocom.app.platform.security.ip;

import org.springframework.util.StringUtils;

/**
 * Textual IPv4/CIDR allow-list matching for {@code IntegrationClient.allowedIps}.
 * Deliberately does not use {@code InetAddress.getByName}, which silently falls
 * back to a live DNS lookup for anything that isn't a strict address literal -
 * entries here are admin-entered but matched on every inbound integration
 * request, so a malformed entry must fail closed instead of blocking the
 * request thread on a network call. IPv4 only: every known/expected caller
 * (labs, HIS, other registries) reaches this API over IPv4.
 */
public final class IpAllowlistMatcher {

    private IpAllowlistMatcher() {
    }

    public static boolean isAllowed(String remoteAddr, String allowedIpsCsv) {
        if (!StringUtils.hasText(allowedIpsCsv)) {
            return true;
        }

        Integer callerAddress = parseAddress(remoteAddr);
        if (callerAddress == null) {
            return false;
        }

        for (String entry : allowedIpsCsv.split(",")) {
            if (matchesEntry(callerAddress, entry.trim())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidEntry(String entry) {
        if (!StringUtils.hasText(entry)) {
            return false;
        }

        String trimmed = entry.trim();
        int slash = trimmed.indexOf('/');
        String addressPart = slash < 0 ? trimmed : trimmed.substring(0, slash);

        if (parseAddress(addressPart) == null) {
            return false;
        }

        return slash < 0 || parsePrefixLength(trimmed.substring(slash + 1)) >= 0;
    }

    private static boolean matchesEntry(int callerAddress, String entry) {
        int slash = entry.indexOf('/');
        String addressPart = slash < 0 ? entry : entry.substring(0, slash);

        Integer networkAddress = parseAddress(addressPart);
        if (networkAddress == null) {
            return false;
        }

        int prefixBits = slash < 0 ? 32 : parsePrefixLength(entry.substring(slash + 1));
        if (prefixBits < 0) {
            return false;
        }

        int mask = prefixBits == 0 ? 0 : (int) (0xFFFFFFFFL << (32 - prefixBits));
        return (callerAddress & mask) == (networkAddress & mask);
    }

    private static int parsePrefixLength(String raw) {
        try {
            int prefix = Integer.parseInt(raw.trim());
            return prefix >= 0 && prefix <= 32 ? prefix : -1;
        } catch (NumberFormatException invalid) {
            return -1;
        }
    }

    private static Integer parseAddress(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String[] octets = text.trim().split("\\.", -1);
        if (octets.length != 4) {
            return null;
        }

        int result = 0;
        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3 || !isDigits(octet)) {
                return null;
            }
            int value = Integer.parseInt(octet);
            if (value > 255) {
                return null;
            }
            result = (result << 8) | value;
        }

        return result;
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
