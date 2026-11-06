package uz.uzinfocom.app.modules.reference.application.neighborhood.query.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NeighborhoodQueryMappingHelperTest {

    @Mock
    private LocalizedTextResolver localizedTextResolver;

    @Mock
    private ReferenceLookupService referenceLookupService;

    @InjectMocks
    private NeighborhoodQueryMappingHelper helper;

    @Test
    void grandParentIsTheRegionOfTheDistrict() {
        when(referenceLookupService.findDistrict("NG-401367"))
                .thenReturn(new ReferenceItem("NG-401367", "UZ-NG", "Uchtepa", null, null, null, null, null, null));
        when(referenceLookupService.getRegionName("UZ-NG")).thenReturn("Namangan viloyati");

        assertThat(helper.neighborhoodGrandParentCode("NG-401367")).isEqualTo("UZ-NG");
        assertThat(helper.neighborhoodGrandParentName("NG-401367")).isEqualTo("Namangan viloyati");
    }

    @Test
    void grandParentIsNullWhenDistrictIsUnknown() {
        when(referenceLookupService.findDistrict("XX-1")).thenReturn(null);

        assertThat(helper.neighborhoodGrandParentCode("XX-1")).isNull();
        assertThat(helper.neighborhoodGrandParentName("XX-1")).isNull();
    }
}
