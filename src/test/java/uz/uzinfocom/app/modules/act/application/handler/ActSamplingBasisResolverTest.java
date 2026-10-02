package uz.uzinfocom.app.modules.act.application.handler;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActSamplingBasisResolverTest {

    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final ActSamplingBasisResolver resolver = new ActSamplingBasisResolver(referenceLookupService);

    @Test
    void blankCodeClearsBasis() {
        assertThat(resolver.resolve(null)).isNull();
        assertThat(resolver.resolve("  ")).isNull();
    }

    @Test
    void knownCodeIsStoredAsCatalogCode() {
        when(referenceLookupService.findCatalog(Act.SAMPLING_BASIS_CATALOG_TYPE, "PLANNED")).thenReturn(
                new ReferenceItem("PLANNED", null, "Rejali", "Режали", "Плановый", "Rejeli", null, null, null)
        );

        assertThat(resolver.resolve("PLANNED")).isEqualTo("PLANNED");
    }

    @Test
    void unknownCodeIsRejected() {
        assertThatThrownBy(() -> resolver.resolve("NOPE"))
                .isInstanceOf(ActValidationException.class);
    }
}
