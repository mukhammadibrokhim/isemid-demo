package uz.uzinfocom.isemid.features.patient.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.form058.models.enums.AddressType;
import uz.uzinfocom.isemid.features.patient.dtos.requests.AddressRequest;
import uz.uzinfocom.isemid.features.patient.models.Address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddressMapperTest {

    private final CatalogMapperHelper catalogMapperHelper = mock(CatalogMapperHelper.class);
    private final AddressMapper addressMapper = new AddressMapperImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(addressMapper, "catalogMapperHelper", catalogMapperHelper);
    }

    @Test
    void toEntity_mapsAllFieldsAndResolvesNeighborhoodCodeThroughHelper() {
        AddressRequest request = AddressRequest.builder()
                .type(AddressType.PERMANENT)
                .stateCode("STATE-1")
                .cityCode("CITY-1")
                .neighborhood("NEIGHBORHOOD-RAW")
                .streetAddress("Main street")
                .houseNumber("12")
                .apartmentNumber("34")
                .build();

        when(catalogMapperHelper.toNeighborhoodCode(eq("NEIGHBORHOOD-RAW"), eq("PATIENT")))
                .thenReturn("NEIGHBORHOOD-CODE");

        Address address = addressMapper.toEntity(request, "PATIENT");

        assertThat(address.getType()).isEqualTo(AddressType.PERMANENT);
        assertThat(address.getStateCode()).isEqualTo("STATE-1");
        assertThat(address.getCityCode()).isEqualTo("CITY-1");
        assertThat(address.getNeighborhoodCode()).isEqualTo("NEIGHBORHOOD-CODE");
        assertThat(address.getStreetAddress()).isEqualTo("Main street");
        assertThat(address.getHouseNumber()).isEqualTo("12");
        assertThat(address.getApartmentNumber()).isEqualTo("34");

        verify(catalogMapperHelper).toNeighborhoodCode("NEIGHBORHOOD-RAW", "PATIENT");
    }

    @Test
    void toEntity_passesSourceContextThroughToNeighborhoodResolution() {
        AddressRequest request = AddressRequest.builder()
                .neighborhood("123456789")
                .build();

        when(catalogMapperHelper.toNeighborhoodCode(eq("123456789"), eq("DMED")))
                .thenReturn("RESOLVED-BY-INN");

        Address address = addressMapper.toEntity(request, "DMED");

        assertThat(address.getNeighborhoodCode()).isEqualTo("RESOLVED-BY-INN");
        verify(catalogMapperHelper).toNeighborhoodCode("123456789", "DMED");
    }

    @Test
    void toEntity_returnsNullNeighborhoodCodeWhenHelperCannotResolveIt() {
        AddressRequest request = AddressRequest.builder()
                .neighborhood("UNKNOWN-INN")
                .build();

        when(catalogMapperHelper.toNeighborhoodCode(eq("UNKNOWN-INN"), eq("DMED")))
                .thenReturn(null);

        Address address = addressMapper.toEntity(request, "DMED");

        assertThat(address.getNeighborhoodCode()).isNull();
    }

    @Test
    void toEntity_returnsNullWhenRequestIsNull() {
        Address address = addressMapper.toEntity(null, "PATIENT");

        assertThat(address).isNull();
    }
}
