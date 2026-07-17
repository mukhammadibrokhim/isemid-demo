package uz.uzinfocom.app.shared.validation;

import org.springframework.util.StringUtils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ReferenceCodeValidation {

    private ReferenceCodeValidation() {
    }

    /**
     * Blank/missing codes are left to bean-validation on the request DTO —
     * this only rejects a code that was actually supplied but does not exist
     * in the reference catalog.
     */
    public static void requireExists(
            String code,
            Predicate<String> existsCheck,
            Supplier<? extends RuntimeException> exceptionSupplier
    ) {
        if (StringUtils.hasText(code) && !existsCheck.test(code)) {
            throw exceptionSupplier.get();
        }
    }
}
