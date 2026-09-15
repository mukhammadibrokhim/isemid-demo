package uz.uzinfocom.app.platform.audit.domain;

import jakarta.persistence.Embeddable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditFieldReflectorTest {

    @Embeddable
    static class Address {
        String city;
        String street;

        Address(String city, String street) {
            this.city = city;
            this.street = street;
        }
    }

    static class ExcludedAssociation {
        Long id;
    }

    static class Boundary {
        // simulates BaseEntity-style infra fields that must never be audited
        Long id;
        Long version;
    }

    static class Base extends Boundary {
        String status;
        LocalDate startDate;
        List<String> tags = new ArrayList<>();
    }

    static class Leaf extends Base {
        Integer count;
        Address address = new Address("Tashkent", "Amir Temur");
        ExcludedAssociation association = new ExcludedAssociation();
        Map<String, String> metadata = Map.of("k", "v");
    }

    @Test
    void includesScalarFieldsFromEntireHierarchyBelowBoundary() {
        Leaf leaf = new Leaf();
        leaf.status = "SENT";
        leaf.startDate = LocalDate.of(2026, 1, 1);
        leaf.count = 5;

        Map<String, Object> fields = AuditFieldReflector.reflect(leaf, Boundary.class);

        assertThat(fields).containsEntry("status", "SENT");
        assertThat(fields).containsEntry("startDate", LocalDate.of(2026, 1, 1));
        assertThat(fields).containsEntry("count", 5);
    }

    @Test
    void excludesFieldsDeclaredAtOrAboveTheBoundary() {
        Leaf leaf = new Leaf();

        Map<String, Object> fields = AuditFieldReflector.reflect(leaf, Boundary.class);

        assertThat(fields).doesNotContainKeys("id", "version");
    }

    @Test
    void excludesCollectionsAndMaps() {
        Leaf leaf = new Leaf();

        Map<String, Object> fields = AuditFieldReflector.reflect(leaf, Boundary.class);

        assertThat(fields).doesNotContainKeys("tags", "metadata");
    }

    @Test
    void excludesEntityAssociationsByDeclaredType() {
        Leaf leaf = new Leaf();

        Map<String, Object> fields = AuditFieldReflector.reflect(leaf, Boundary.class);

        assertThat(fields).doesNotContainKey("association");
    }

    @Test
    void flattensEmbeddableFieldsWithDotPrefix() {
        Leaf leaf = new Leaf();

        Map<String, Object> fields = AuditFieldReflector.reflect(leaf, Boundary.class);

        assertThat(fields).containsEntry("address.city", "Tashkent");
        assertThat(fields).containsEntry("address.street", "Amir Temur");
    }

    @Test
    void returnsEmptyMapForNullEntity() {
        assertThat(AuditFieldReflector.reflect(null, Boundary.class)).isEmpty();
    }
}
