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

class ActActivityTypeResolverTest {

    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final ActActivityTypeResolver resolver = new ActActivityTypeResolver(referenceLookupService);

    @Test
    void blankCodeClearsActivityType() {
        assertThat(resolver.resolve(null)).isNull();
        assertThat(resolver.resolve("  ")).isNull();
    }

    @Test
    void knownCodeIsStoredAsCatalogCode() {
        when(referenceLookupService.findCatalog(Act.ACTIVITY_TYPE_CATALOG_TYPE, "ACT_SCHOOL_EDU")).thenReturn(
                new ReferenceItem("ACT_SCHOOL_EDU", null, "Maktab", "Мактаб", "Школа", "Mektep", null, null, null)
        );

        assertThat(resolver.resolve("ACT_SCHOOL_EDU")).isEqualTo("ACT_SCHOOL_EDU");
    }

    @Test
    void unknownCodeIsRejected() {
        assertThatThrownBy(() -> resolver.resolve("NOPE"))
                .isInstanceOf(ActValidationException.class);
    }
}
