package uz.uzinfocom.app.platform.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class I18nMessageBundleTest {

    private static final Path I18N_DIR = Path.of("src/main/resources/i18n");
    private static final Path MAIN_JAVA_DIR = Path.of("src/main/java");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)}");

    private static final List<String> BUNDLE_FILES = List.of(
            "messages.properties",
            "messages_en.properties",
            "messages_uz.properties",
            "messages_ru.properties",
            "messages_uz_Cyrl.properties",
            "messages_kaa.properties"
    );

    private static final List<String> OLD_MESSAGE_KEYS = List.of(
            "error.internal",
            "error.validation",
            "error.resource_not_found",
            "error.method_not_allowed",
            "error.scope_violation",
            "error.organization_required",
            "error.organization_invalid",
            "error.organization_not_allowed",
            "error.user.not_found",
            "error.role.not_found",
            "error.permission.not_found",
            "error.permission.ids_not_found",
            "validation.permission.ids.required",
            "validation.permission.id.required",
            "validation.role.id.required",
            "permission.subject.already.exists",
            "permission.not.found",
            "role.not.found",
            "role.name.already.exists",
            "role.update.deleted.conflict",
            "role.not.available",
            "permission.update.deleted.conflict",
            "permission.not.available",
            "role.permission.manage.remove.conflict",
            "scope.current"
    );

    @Test
    void localeBundlesHaveIdenticalKeysAndValidValues() throws IOException {
        Map<String, Bundle> bundles = loadBundles();
        Bundle defaultBundle = bundles.get("messages.properties");

        for (Bundle bundle : bundles.values()) {
            assertThat(bundle.duplicateKeys)
                    .as("duplicate keys in %s", bundle.fileName)
                    .isEmpty();
            assertThat(bundle.emptyKeys)
                    .as("empty values in %s", bundle.fileName)
                    .isEmpty();
            assertThat(bundle.todoKeys)
                    .as("unfinished values in %s", bundle.fileName)
                    .isEmpty();
            assertThat(bundle.keys())
                    .as("key set in %s", bundle.fileName)
                    .containsExactlyElementsOf(defaultBundle.keys());
        }
    }

    @Test
    void localeBundlesUseMatchingPlaceholders() throws IOException {
        Map<String, Bundle> bundles = loadBundles();
        Bundle defaultBundle = bundles.get("messages.properties");

        for (String key : defaultBundle.keys()) {
            Set<String> defaultPlaceholders = placeholders(defaultBundle.value(key));

            for (Bundle bundle : bundles.values()) {
                assertThat(placeholders(bundle.value(key)))
                        .as("placeholder mismatch for %s in %s", key, bundle.fileName)
                        .isEqualTo(defaultPlaceholders);
            }
        }
    }

    @Test
    void oldMessageKeysAreRemovedFromMainCodeAndBundles() throws IOException {
        Map<String, Bundle> bundles = loadBundles();

        for (String oldKey : OLD_MESSAGE_KEYS) {
            for (Bundle bundle : bundles.values()) {
                assertThat(bundle.keys())
                        .as("old key %s in %s", oldKey, bundle.fileName)
                        .doesNotContain(oldKey);
            }
        }

        String mainJava = readMainJava();
        for (String oldKey : OLD_MESSAGE_KEYS) {
            assertThat(mainJava)
                    .as("old key literal %s in main Java sources", oldKey)
                    .doesNotContain("\"" + oldKey + "\"")
                    .doesNotContain("\"{" + oldKey + "}\"");
        }
    }

    @Test
    void canonicalKeysResolveForSupportedLocales() {
        MessageSource messageSource = new I18nConfig().messageSource();
        List<Locale> locales = List.of(
                Locale.ENGLISH,
                Locale.forLanguageTag("uz"),
                Locale.forLanguageTag("ru"),
                Locale.forLanguageTag("uz-Cyrl"),
                Locale.forLanguageTag("kaa")
        );

        for (Locale locale : locales) {
            assertThat(messageSource.getMessage("common.success", null, locale))
                    .as("common.success in %s", locale)
                    .isNotBlank()
                    .isNotEqualTo("common.success");
            assertThat(messageSource.getMessage("permission.not_found_by_id", new Object[]{150}, locale))
                    .as("permission.not_found_by_id in %s", locale)
                    .contains("150")
                    .doesNotContain("permission.not_found_by_id");
            assertThat(messageSource.getMessage("organization.scope_violation", null, locale))
                    .as("organization.scope_violation in %s", locale)
                    .isNotBlank()
                    .isNotEqualTo("organization.scope_violation");
            assertThat(messageSource.getMessage("error.method_not_supported", null, locale))
                    .as("error.method_not_supported in %s", locale)
                    .isNotBlank()
                    .isNotEqualTo("error.method_not_supported");
        }
    }

    @Test
    void unsupportedLocaleFallsBackToUz() {
        LocaleResolver localeResolver = new I18nConfig().localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "fr-FR,fr;q=0.9");

        assertThat(localeResolver.resolveLocale(request)).isEqualTo(Locale.forLanguageTag("uz"));
    }

    private static Map<String, Bundle> loadBundles() throws IOException {
        Map<String, Bundle> bundles = new LinkedHashMap<>();
        for (String fileName : BUNDLE_FILES) {
            Path path = I18N_DIR.resolve(fileName);
            assertThat(path).as("bundle file %s", fileName).exists();
            bundles.put(fileName, Bundle.load(path, fileName));
        }
        return bundles;
    }

    private static String readMainJava() throws IOException {
        try (Stream<Path> paths = Files.walk(MAIN_JAVA_DIR)) {
            StringBuilder builder = new StringBuilder();
            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList()) {
                builder.append(Files.readString(path, StandardCharsets.UTF_8)).append('\n');
            }
            return builder.toString();
        }
    }

    private static Set<String> placeholders(String value) {
        return PLACEHOLDER_PATTERN.matcher(value)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private record Bundle(
            String fileName,
            Properties properties,
            Set<String> duplicateKeys,
            Set<String> emptyKeys,
            Set<String> todoKeys
    ) {

        static Bundle load(Path path, String fileName) throws IOException {
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            Set<String> duplicateKeys = new TreeSet<>();
            Set<String> emptyKeys = new TreeSet<>();
            Set<String> todoKeys = new TreeSet<>();
            Set<String> seenKeys = new TreeSet<>();

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                    continue;
                }

                int separator = line.indexOf('=');
                assertThat(separator)
                        .as("missing '=' separator in %s line: %s", fileName, line)
                        .isGreaterThan(0);

                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();

                if (!seenKeys.add(key)) {
                    duplicateKeys.add(key);
                }
                if (value.isEmpty()) {
                    emptyKeys.add(key);
                }
                if (value.toLowerCase(Locale.ROOT).contains("todo")) {
                    todoKeys.add(key);
                }
            }

            assertThat(properties.stringPropertyNames())
                    .as("parsed keys in %s", fileName)
                    .containsExactlyInAnyOrderElementsOf(seenKeys);

            return new Bundle(fileName, properties, duplicateKeys, emptyKeys, todoKeys);
        }

        Set<String> keys() {
            return properties.stringPropertyNames()
                    .stream()
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        String value(String key) {
            return properties.getProperty(key);
        }
    }
}
