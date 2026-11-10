package uz.uzinfocom.app.platform.export.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.excel.ExcelStyleSettings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the dev-panel-configurable column selection/order and visual style for a given
 * {@link ExcelExportSource}, via the existing runtime {@code system_settings} key/value
 * store (same table backing {@code /v1/admin/settings} and {@code /v1/dev/settings}).
 * Nothing configured for a key is never a behavior change - see
 * {@code SystemSettingResolver}'s own contract - so every export works out of the box with
 * the source's own defaults until someone deliberately edits a setting.
 */
@Component
@RequiredArgsConstructor
public class ExcelExportSettingsResolver {

    private static final String KEY_PREFIX = "export.excel.";

    private final SystemSettingResolver systemSettingResolver;

    public <T> List<ExcelColumn<T>> resolveColumns(ExcelExportSource<?, T> source) {
        List<ExcelColumn<T>> available = source.availableColumns();

        List<String> defaultKeys = available.stream().map(ExcelColumn::key).toList();
        List<String> configuredKeys = systemSettingResolver.resolveStringList(columnsKey(source.exportType()), defaultKeys);

        Map<String, ExcelColumn<T>> byKey = available.stream()
                .collect(Collectors.toMap(ExcelColumn::key, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        List<ExcelColumn<T>> resolved = configuredKeys.stream()
                .map(byKey::get)
                .filter(Objects::nonNull)
                .toList();

        return resolved.isEmpty() ? available : resolved;
    }

    public ExcelStyleSettings resolveStyle(ExcelExportSource<?, ?> source) {
        ExcelStyleSettings defaults = source.defaultStyle();
        String prefix = stylePrefix(source.exportType());

        String headerFillColorHex = systemSettingResolver.resolveString(prefix + "header-fill-color", defaults.headerFillColorHex());
        String fontName = systemSettingResolver.resolveString(prefix + "font-name", defaults.fontName());
        long fontSize = systemSettingResolver.resolveLong(prefix + "font-size", defaults.fontSize());
        boolean headerBold = systemSettingResolver.resolveBoolean(prefix + "header-bold", defaults.headerBold());
        boolean thinBorder = systemSettingResolver.resolveBoolean(prefix + "thin-border", defaults.thinBorder());
        long rowsPerSheet = systemSettingResolver.resolveLong(prefix + "rows-per-sheet", defaults.rowsPerSheet());

        return new ExcelStyleSettings(headerFillColorHex, fontName, (int) fontSize, headerBold, thinBorder, (int) rowsPerSheet);
    }

    private String columnsKey(String exportType) {
        return KEY_PREFIX + exportType.toLowerCase() + ".columns";
    }

    private String stylePrefix(String exportType) {
        return KEY_PREFIX + exportType.toLowerCase() + ".style.";
    }
}
