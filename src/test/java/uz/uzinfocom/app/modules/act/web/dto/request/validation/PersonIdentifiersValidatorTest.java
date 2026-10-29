package uz.uzinfocom.app.modules.act.web.dto.request.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.modules.act.web.dto.request.Act156Request;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156GroupDetailRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156KitchenUtensilRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PersonIdentifiersValidatorTest {

    private static final String VALID_NNUZB = "51506123456785";

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsMissingAndCompletePairs() {
        Act156Request request = request(
                List.of(chef(null, null), chef(CitizenLookupType.NNUZB, VALID_NNUZB), chef(CitizenLookupType.PPN, "AA1234567")),
                List.of(group(null, null, CitizenLookupType.CZ, "I-TN 123456"))
        );

        assertThat(violations(request)).isEmpty();
    }

    @Test
    void valueWithoutTypeIsReportedOnTheTypeField() {
        Act156Request request = request(List.of(chef(null, null), chef(null, VALID_NNUZB)), List.of());

        assertThat(violations(request))
                .containsExactly(Map.entry("kitchenUtensils[1].identifierTypeOfChef", "{validation.identifier.type.required}"));
    }

    @Test
    void typeWithoutValueIsReportedOnTheValueField() {
        Act156Request request = request(List.of(chef(CitizenLookupType.PPN, "  ")), List.of());

        assertThat(violations(request))
                .containsExactly(Map.entry("kitchenUtensils[0].identifierValueOfChef", "{validation.identifier.value.required}"));
    }

    @Test
    void nnuzbMustBeExactly14Digits() {
        Act156Request request = request(List.of(chef(CitizenLookupType.NNUZB, "1234567890123")), List.of());

        assertThat(violations(request))
                .containsExactly(Map.entry("kitchenUtensils[0].identifierValueOfChef", "{validation.nnuzb.format}"));
    }

    @Test
    void groupDetailReportsEachPersonSeparately() {
        Act156Request request = request(
                List.of(),
                List.of(group(null, null, null, null), group(null, VALID_NNUZB, CitizenLookupType.NNUZB, "12ab"))
        );

        assertThat(violations(request)).containsOnly(
                Map.entry("groupDetails[1].identifierTypeOfEducator", "{validation.identifier.type.required}"),
                Map.entry("groupDetails[1].identifierValueOfPlaceOwner", "{validation.nnuzb.format}")
        );
    }

    @Test
    void objectRepresentativeFollowsTheSameRules() {
        assertThat(violations(representative(CitizenLookupType.NNUZB, VALID_NNUZB))).isEmpty();
        assertThat(violations(representative(null, null))).isEmpty();
        assertThat(violations(representative(null, "AA1234567")))
                .containsExactly(Map.entry("identifierTypeOfObjectRepresentative", "{validation.identifier.type.required}"));
        assertThat(violations(representative(CitizenLookupType.PPN, null)))
                .containsExactly(Map.entry("identifierValueOfObjectRepresentative", "{validation.identifier.value.required}"));
        assertThat(violations(representative(CitizenLookupType.NNUZB, "123")))
                .containsExactly(Map.entry("identifierValueOfObjectRepresentative", "{validation.nnuzb.format}"));
    }

    private static Act156Request representative(CitizenLookupType type, String value) {
        return new Act156Request(null, null, null, null, null, null, null, "Rep", null, type, value, List.of(), List.of());
    }

    private static Map<String, String> violations(Act156Request request) {
        return validator.validate(request).stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessageTemplate));
    }

    private static Act156Request request(List<Act156KitchenUtensilRequest> utensils, List<Act156GroupDetailRequest> groups) {
        return new Act156Request(null, null, null, null, null, null, null, null, null, null, null, utensils, groups);
    }

    private static Act156KitchenUtensilRequest chef(CitizenLookupType type, String value) {
        return new Act156KitchenUtensilRequest(null, null, null, null, null, "Chef", type, value, null, null);
    }

    private static Act156GroupDetailRequest group(
            CitizenLookupType educatorType, String educatorValue,
            CitizenLookupType placeOwnerType, String placeOwnerValue
    ) {
        return new Act156GroupDetailRequest(
                null, "5-A", "Educator", educatorType, educatorValue, null,
                null, null, null, null, null, null, null, null, null, null,
                "Owner", placeOwnerType, placeOwnerValue,
                null, null, null, null, null, null
        );
    }
}
