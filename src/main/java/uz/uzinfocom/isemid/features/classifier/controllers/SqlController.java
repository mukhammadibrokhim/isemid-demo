package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Db.ROOT)
@Hidden
@RequiredArgsConstructor
public class SqlController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/sql")
    public ResponseEntity<?> runQuery(@RequestBody Map<String, String> body) {

        String sql = body.get("query");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query is empty");
        }

        try {
            return ResponseEntity.ok(
                    jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {

                        boolean hasResultSet = ps.execute();

                        Map<String, Object> response = new LinkedHashMap<>();

                        if (hasResultSet) {
                            ResultSet rs = ps.getResultSet();
                            ResultSetMetaData md = rs.getMetaData();

                            List<Map<String, Object>> rows = new ArrayList<>();

                            while (rs.next()) {
                                Map<String, Object> row = new LinkedHashMap<>();
                                for (int i = 1; i <= md.getColumnCount(); i++) {
                                    row.put(md.getColumnLabel(i), rs.getObject(i));
                                }
                                rows.add(row);
                            }

                            response.put("type", "SELECT");
                            response.put("rowCount", rows.size());
                            response.put("data", rows);

                        } else {
                            int updateCount = ps.getUpdateCount();

                            response.put("type", "UPDATE");
                            response.put("affectedRows", updateCount);
                        }

                        return response;
                    })
            );

        } catch (Exception e) {

            Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);

            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "error", root.getMessage()
                    )
            );
        }
    }
}