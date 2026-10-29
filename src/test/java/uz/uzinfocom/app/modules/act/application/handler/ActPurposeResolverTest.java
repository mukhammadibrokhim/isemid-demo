package uz.uzinfocom.app.modules.act.application.handler;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActPurposeResolverTest {

    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final ActPurposeResolver resolver = new ActPurposeResolver(referenceLookupService);

    @Test
    void blankCodeClearsPurpose() {
        assertThat(resolver.resolve(null)).isNull();
        assertThat(resolver.resolve("  ")).isNull();
    }

    @Test
    void knownCodeIsStoredAsCatalogCode() {
        when(referenceLookupService.findCatalog(Purpose.CATALOG_TYPE, "1")).thenReturn(
                new ReferenceItem("1", null, "Rejali", "Режали", "Плановый", "Rejeli", null, null, null)
        );

        Purpose purpose = resolver.resolve("1");

        assertThat(purpose.getCode()).isEqualTo("1");
        assertThat(purpose.getSamplingPurposeUz()).isNull();
    }

    @Test
    void legacySnapshotSurvivesASaveWithoutCode() {
        Purpose legacy = new Purpose(null, 7, "Rejali", "Плановый", "LP-1");

        assertThat(resolver.resolve(null, legacy)).isSameAs(legacy);
    }

    @Test
    void pickingACodeReplacesLegacySnapshot() {
        when(referenceLookupService.findCatalog(Purpose.CATALOG_TYPE, "1")).thenReturn(
                new ReferenceItem("1", null, "Rejali", "Режали", "Плановый", "Rejeli", null, null, null)
        );
        Purpose legacy = new Purpose(null, 7, "Rejali", "Плановый", "LP-1");

        Purpose purpose = resolver.resolve("1", legacy);

        assertThat(purpose.getCode()).isEqualTo("1");
        assertThat(purpose.getPurposeId()).isNull();
    }

    @Test
    void codedPurposeIsClearedByBlankCode() {
        assertThat(resolver.resolve(null, Purpose.ofCode("1"))).isNull();
    }

    @Test
    void unknownCodeIsRejected() {
        assertThatThrownBy(() -> resolver.resolve("NOPE"))
                .isInstanceOf(ActValidationException.class);
    }
}
