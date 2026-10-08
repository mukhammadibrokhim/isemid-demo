package uz.uzinfocom.app.platform.http.ssrf;

import org.apache.hc.client5.http.DnsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboundAddressPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1", "127.8.9.10", "0.0.0.0", "0.1.2.3", "169.254.169.254", "224.0.0.1",
            "255.255.255.255", "240.0.0.1", "::1", "::", "fe80::1", "ff02::1", "::127.0.0.1",
            "64:ff9b::a9fe:a9fe"
    })
    void alwaysForbidsLoopbackLinkLocalAndReservedAddresses(String literal) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(literal);

        assertThat(OutboundAddressPolicy.isForbidden(address, false)).isTrue();
        assertThat(OutboundAddressPolicy.isForbidden(address, true)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "10.0.0.1", "172.16.0.1", "172.31.255.255", "192.168.0.1", "100.64.0.1", "fd12:3456::1", "fec0::1"
    })
    void forbidsPrivateNetworksUnlessAllowed(String literal) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(literal);

        assertThat(OutboundAddressPolicy.isForbidden(address, false)).isTrue();
        assertThat(OutboundAddressPolicy.isForbidden(address, true)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"8.8.8.8", "172.32.0.1", "100.128.0.1", "2001:4860:4860::8888"})
    void allowsPublicAddresses(String literal) throws UnknownHostException {
        assertThat(OutboundAddressPolicy.isForbidden(InetAddress.getByName(literal), false)).isFalse();
    }

    @Test
    void hostCheckRejectsLocalhostNamesAndBracketedIpv6Literals() {
        assertThat(OutboundAddressPolicy.isForbiddenHost("localhost", true)).isTrue();
        assertThat(OutboundAddressPolicy.isForbiddenHost("api.localhost", true)).isTrue();
        assertThat(OutboundAddressPolicy.isForbiddenHost("[::1]", true)).isTrue();
        assertThat(OutboundAddressPolicy.isForbiddenHost("", true)).isTrue();
        assertThat(OutboundAddressPolicy.isForbiddenHost(null, true)).isTrue();
    }

    @Test
    void hostCheckLetsAnUnresolvableHostThroughForTheConnectTimeGuard() {
        assertThat(OutboundAddressPolicy.isForbiddenHost("no-such-host.invalid", false)).isFalse();
    }

    @Test
    void resolverFailsTheLookupWhenAnyResolvedAddressIsForbidden() throws UnknownHostException {
        DnsResolver delegate = mock(DnsResolver.class);
        when(delegate.resolve("rebind.example")).thenReturn(new InetAddress[]{
                InetAddress.getByName("8.8.8.8"), InetAddress.getByName("127.0.0.1")
        });

        GuardedDnsResolver resolver = new GuardedDnsResolver(delegate, false);

        assertThatThrownBy(() -> resolver.resolve("rebind.example")).isInstanceOf(UnknownHostException.class);
    }

    @Test
    void resolverReturnsPublicAddressesUnchanged() throws UnknownHostException {
        DnsResolver delegate = mock(DnsResolver.class);
        InetAddress[] addresses = {InetAddress.getByName("8.8.8.8")};
        when(delegate.resolve("partner.example")).thenReturn(addresses);

        assertThat(new GuardedDnsResolver(delegate, false).resolve("partner.example")).isEqualTo(addresses);
    }
}
