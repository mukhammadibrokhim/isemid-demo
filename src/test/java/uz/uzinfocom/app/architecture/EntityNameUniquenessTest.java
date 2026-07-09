package uz.uzinfocom.app.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hibernate resolves an unqualified JPA entity name from the bare simple
 * class name unless {@code @Entity(name = "...")} overrides it — two
 * {@code @Entity} classes with the same simple name collide even if they
 * live in entirely different packages, and {@code EntityManagerFactory}
 * creation fails at application startup with
 * {@code DuplicateMappingException}.
 * <p>
 * This exact bug shipped once ({@code card161.InfectionSource} vs.
 * {@code card_tube.InfectionSource}) and was invisible to every other test
 * in this project, since none of them boot a real Hibernate
 * {@code SessionFactory} against a database. This test catches it
 * statically instead, without needing a DB connection.
 */
class EntityNameUniquenessTest {

    @Test
    void noTwoEntityClassesShareTheSameJpaEntityName() {
        JavaClasses classes = new ClassFileImporter().importPackages("uz.uzinfocom.app");

        Map<String, List<String>> classesByEntityName = classes.stream()
                .filter(javaClass -> javaClass.isAnnotatedWith(Entity.class))
                .collect(Collectors.groupingBy(
                        EntityNameUniquenessTest::resolveEntityName,
                        Collectors.mapping(JavaClass::getFullName, Collectors.toList())
                ));

        List<Map.Entry<String, List<String>>> duplicates = classesByEntityName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .toList();

        assertThat(duplicates)
                .as("Each @Entity class must have a unique JPA entity name (simple class name, or an explicit @Entity(name=...))")
                .isEmpty();
    }

    private static String resolveEntityName(JavaClass javaClass) {
        Entity entity = javaClass.reflect().getAnnotation(Entity.class);
        return StringUtils.hasText(entity.name()) ? entity.name() : javaClass.getSimpleName();
    }
}
