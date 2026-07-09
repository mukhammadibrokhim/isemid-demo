package uz.uzinfocom.app.modules.card.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bare {@code String} field as a reference to an entry in the
 * generic catalog system (type + code — see {@code platform.reference}),
 * not yet backed by a typed enum or dedicated reference entity. Purely
 * documentation: makes every such field greppable by catalog name so future
 * catalog-integration work doesn't have to rediscover them by reading every
 * entity.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CatalogCode {

    /**
     * The catalog "type" this code is expected to belong to (see
     * {@code Catalog.type} in {@code platform.reference}).
     */
    String value();
}
