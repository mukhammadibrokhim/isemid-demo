package uz.uzinfocom.app.platform.devpanel.application.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevSqlConsoleServiceTest {

    @Test
    void acceptsReadStatementsAndStripsTrailingSemicolons() {
        assertThat(DevSqlConsoleService.requireSingleReadStatement("  select 1 ;; ")).isEqualTo("select 1");
        assertThat(DevSqlConsoleService.requireSingleReadStatement("WITH x AS (select 1) select * from x"))
                .startsWith("WITH");
        assertThat(DevSqlConsoleService.requireSingleReadStatement("(select 1)")).isEqualTo("(select 1)");
        assertThat(DevSqlConsoleService.requireSingleReadStatement("explain select 1")).isEqualTo("explain select 1");
    }

    @Test
    void rejectsSecondStatementThatCouldEndTheReadOnlyTransaction() {
        assertThatThrownBy(() -> DevSqlConsoleService.requireSingleReadStatement("select 1; commit; delete from x"))
                .isInstanceOf(DevSqlRejectedException.class);
        assertThatThrownBy(() -> DevSqlConsoleService.requireSingleReadStatement("select 1 /* ' */ ; commit"))
                .isInstanceOf(DevSqlRejectedException.class);
    }

    @Test
    void rejectsWritesDdlAndExplainAnalyze() {
        for (String sql : new String[]{"delete from x", "update x set a=1", "drop table x", "insert into x values (1)",
                "explain analyze select 1", "explain (analyze, buffers) select 1", "", "   ;  "}) {
            assertThatThrownBy(() -> DevSqlConsoleService.requireSingleReadStatement(sql))
                    .as(sql).isInstanceOf(DevSqlRejectedException.class);
        }
    }
}
