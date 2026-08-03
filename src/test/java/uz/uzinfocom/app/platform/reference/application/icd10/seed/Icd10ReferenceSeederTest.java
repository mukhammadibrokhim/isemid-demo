package uz.uzinfocom.app.platform.reference.application.icd10.seed;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import uz.uzinfocom.app.platform.reference.domain.Icd10;
import uz.uzinfocom.app.platform.reference.repository.Icd10Repository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

class Icd10ReferenceSeederTest {

    private final Icd10Repository icd10Repository = mock(Icd10Repository.class);
    private final Icd10ReferenceSeeder seeder = new Icd10ReferenceSeeder(icd10Repository);

    @Test
    void splitsPlainCommaSeparatedLine() {
        List<String> fields = Icd10ReferenceSeeder.splitCsvLine("21,uuid-1,2,26,(B95-B97),2,false,1,name,name,name,name,");

        assertThat(fields).containsExactly(
                "21", "uuid-1", "2", "26", "(B95-B97)", "2", "false", "1", "name", "name", "name", "name", ""
        );
    }

    @Test
    void splitsQuotedFieldContainingComma() {
        List<String> fields = Icd10ReferenceSeeder.splitCsvLine(
                "313,uuid-2,310,316,A03.2,4,true,1,plain,plain,\"Shigellez, vyzvanny\",plain,"
        );

        assertThat(fields.get(10)).isEqualTo("Shigellez, vyzvanny");
    }

    @Test
    void splitsFieldWithEscapedDoubleQuote() {
        List<String> fields = Icd10ReferenceSeeder.splitCsvLine(
                "2537,uuid-3,2536,2540,E71.0,4,true,1,plain,plain,\"Bolezn \"\"klenovogo siropa\"\"\",plain,"
        );

        assertThat(fields.get(10)).isEqualTo("Bolezn \"klenovogo siropa\"");
    }

    @Test
    void parsesRowWithNullParentAndComment() {
        UUID uuid = UUID.randomUUID();
        Icd10ReferenceSeeder.Icd10Row row = Icd10ReferenceSeeder.Icd10Row.parse(List.of(
                "1", uuid.toString(), "", "10", "A00", "1", "false", "1", "uz", "cyr", "ru", "kaa", ""
        ));

        assertThat(row.id()).isEqualTo(1L);
        assertThat(row.uuid()).isEqualTo(uuid);
        assertThat(row.parentId()).isNull();
        assertThat(row.secondaryId()).isEqualTo(10L);
        assertThat(row.comment()).isNull();
    }

    @Test
    void parsesRowWithParentAndComment() {
        Icd10ReferenceSeeder.Icd10Row row = Icd10ReferenceSeeder.Icd10Row.parse(List.of(
                "2", UUID.randomUUID().toString(), "1", "20", "A00.0", "2", "true", "1", "uz", "cyr", "ru", "kaa", "note"
        ));

        assertThat(row.parentId()).isEqualTo(1L);
        assertThat(row.comment()).isEqualTo("note");
    }

    @Test
    void rowToEntityMapsAllFieldsAndDefaultsDeletedFalse() {
        UUID uuid = UUID.randomUUID();
        Icd10ReferenceSeeder.Icd10Row row = Icd10ReferenceSeeder.Icd10Row.parse(List.of(
                "5", uuid.toString(), "", "50", "B00", "1", "true", "2", "uz-name", "cyr-name", "ru-name", "kaa-name", "c"
        ));

        Icd10 entity = row.toEntity();

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getSecondaryId()).isEqualTo(50L);
        assertThat(entity.getCode()).isEqualTo("B00");
        assertThat(entity.getLevel()).isEqualTo(1);
        assertThat(entity.isLastLevel()).isTrue();
        assertThat(entity.getUsageLimit()).isEqualTo(2);
        assertThat(entity.getNameUz()).isEqualTo("uz-name");
        assertThat(entity.getComment()).isEqualTo("c");
        assertThat(entity.getParent()).isNull();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void skipsSeedingWhenTableIsNotEmpty() throws Exception {
        when(icd10Repository.count()).thenReturn(5L);

        seeder.run(mock(ApplicationArguments.class));

        verify(icd10Repository, never()).saveAllAndFlush(anyIterable());
    }

    @Test
    @SuppressWarnings("unchecked")
    void seedsFromBundledCsvAndWiresParentLinksWhenTableIsEmpty() throws Exception {
        when(icd10Repository.count()).thenReturn(0L);

        seeder.run(mock(ApplicationArguments.class));

        ArgumentCaptor<List<Icd10>> captor = ArgumentCaptor.forClass(List.class);
        verify(icd10Repository).saveAllAndFlush(captor.capture());
        List<Icd10> saved = captor.getValue();

        assertThat(saved).hasSize(14490);

        Icd10 child = saved.stream().filter(n -> n.getId() == 435L).findFirst().orElseThrow();
        assertThat(child.getCode()).isEqualTo("A24.0");
        assertThat(child.getParent()).isNotNull();
        assertThat(child.getParent().getId()).isEqualTo(434L);

        long rootCount = saved.stream().filter(n -> n.getParent() == null).count();
        assertThat(rootCount).isGreaterThanOrEqualTo(22L);
    }
}
