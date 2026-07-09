package uz.uzinfocom.app.modules.card;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The Card module was rebuilt from the legacy {@code uz.uzinfocom.isemid}
 * codebase using it only as a field/relationship spec — none of its
 * architecture (or bugs) may be imported back in.
 */
class CardModuleBoundaryTest {

    private static final String CARD_MODULE_PACKAGE = "uz.uzinfocom.app.modules.card..";
    private static final String LEGACY_ISEMID_PACKAGE = "uz.uzinfocom.isemid..";

    @Test
    void cardModuleMustNotDependOnLegacyIsemidPackages() {
        JavaClasses classes = new ClassFileImporter().importPackages("uz.uzinfocom.app.modules.card");

        ArchRule rule = noClasses()
                .that().resideInAPackage(CARD_MODULE_PACKAGE)
                .should().dependOnClassesThat().resideInAPackage(LEGACY_ISEMID_PACKAGE);

        rule.check(classes);
    }
}
