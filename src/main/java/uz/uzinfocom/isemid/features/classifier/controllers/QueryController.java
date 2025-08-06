package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.ApiPaths;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Db.ROOT)
@Hidden
@RequiredArgsConstructor
public class QueryController {
    private final JdbcTemplate jdbcTemplate;

    @PostMapping
    public ResponseEntity<?> runQuery(
            @RequestBody Map<String, String> body
    ) {
        String query = body.get("query");
        if (query == null)
            return ResponseEntity.badRequest().body("Query cannot be null");
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
