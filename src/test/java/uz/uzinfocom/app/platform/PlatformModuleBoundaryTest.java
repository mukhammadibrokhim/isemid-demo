package uz.uzinfocom.app.platform;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * {@code platform} is cross-cutting infrastructure business modules depend
 * on, never the other way around. Code that legitimately needs to read
 * across module boundaries (dashboards, notifications, webhooks, scope
 * resolution) lives in {@code orchestration}, not {@code platform} — see
 * that package for the cases this rule was written to catch.
 *
 * <p>Two deliberate exceptions:
 * <ul>
 *   <li>{@code modules.reference} is a dependency-free, business-logic-free
 *   reference-data lookup module (country/region/district/ICD-10/catalog
 *   code → display name). {@code modules.iam}'s organization table mapper
 *   resolves region/district names through it
 *   ({@code OrganizationQueryMapper}); that single, narrow read is allowed
 *   rather than forcing a split of an otherwise cohesive mapper.
 *   <li>{@code modules.iam} is itself a dependency-free {@code User}/
 *   {@code Organization}/{@code Role}/{@code Permission}/{@code Action}
 *   domain — the same shape as {@code modules.reference} — that many
 *   {@code platform} packages legitimately need to read (security,
 *   persistence, cache, settings, integrationclient, audit, export). It's
 *   filed under {@code modules} for its own CRUD/domain layering, not
 *   because it's a business capability {@code platform} should be walled
 *   off from.
 * </ul>
 */
class PlatformModuleBoundaryTest {

    private static final String PLATFORM_PACKAGE = "uz.uzinfocom.app.platform..";
    private static final String MODULES_PACKAGE = "uz.uzinfocom.app.modules..";
    private static final String REFERENCE_PACKAGE = "uz.uzinfocom.app.modules.reference..";
    private static final String IAM_PACKAGE = "uz.uzinfocom.app.modules.iam..";

    @Test
    void platformMustNotDependOnModules() {
        JavaClasses classes = new ClassFileImporter().importPackages("uz.uzinfocom.app.platform");

        DescribedPredicate<JavaClass> businessModules = resideInAPackage(MODULES_PACKAGE)
                .and(DescribedPredicate.not(resideInAPackage(REFERENCE_PACKAGE)))
                .and(DescribedPredicate.not(resideInAPackage(IAM_PACKAGE)))
                .as("reside in a business module package (excluding the reference-data lookup module and iam)");

        ArchRule rule = noClasses()
                .that().resideInAPackage(PLATFORM_PACKAGE)
                .should().dependOnClassesThat(businessModules);

        rule.check(classes);
    }
}
