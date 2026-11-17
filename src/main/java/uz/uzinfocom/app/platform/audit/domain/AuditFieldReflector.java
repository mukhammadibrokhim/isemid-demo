package uz.uzinfocom.app.platform.audit.domain;

import jakarta.persistence.Embeddable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reflection-based default for {@link AuditableFields#auditFields()},
 * for entities with too many fields (or too many JOINED-inheritance
 * subtypes, e.g. {@code Card}/{@code Act}) to hand-list like
 * {@code Form058.auditFields()} does. Walks the entity's runtime class
 * hierarchy up to (but excluding) {@code boundaryExclusive}, collecting
 * scalar fields directly and recursing into {@code @Embeddable} fields
 * (flattened as {@code fieldName.leafName}); collections/maps and
 * entity-typed associations are skipped by their *declared* type, so a
 * lazy association is never actually accessed.
 */
public final class AuditFieldReflector {

    private AuditFieldReflector() {
    }

    public static Map<String, Object> reflect(Object entity, Class<?> boundaryExclusive) {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (entity != null) {
            collect(entity, entity.getClass(), boundaryExclusive, "", fields);
        }
        return fields;
    }

    private static void collect(Object instance, Class<?> type, Class<?> boundaryExclusive, String prefix, Map<String, Object> out) {
        if (type == null || type == boundaryExclusive || type == Object.class) {
            return;
        }
        collect(instance, type.getSuperclass(), boundaryExclusive, prefix, out);

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            Class<?> fieldType = field.getType();
            if (Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
                continue;
            }
            field.setAccessible(true);
            if (fieldType.isAnnotationPresent(Embeddable.class)) {
                Object embedded = read(field, instance);
                if (embedded != null) {
                    collect(embedded, fieldType, Object.class, prefix + field.getName() + ".", out);
                }
            } else if (isScalar(fieldType)) {
                out.put(prefix + field.getName(), read(field, instance));
            }
        }
    }

    private static boolean isScalar(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || Temporal.class.isAssignableFrom(type)
                || UUID.class == type;
    }

    private static Object read(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read field " + field + " for audit", e);
        }
    }
}
