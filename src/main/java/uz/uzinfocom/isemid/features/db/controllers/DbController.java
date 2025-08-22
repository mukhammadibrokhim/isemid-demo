package uz.uzinfocom.isemid.features.db.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.features.db.dtos.TableDto;
import uz.uzinfocom.isemid.features.db.services.DbService;

import java.util.List;
import java.util.Map;

import static uz.uzinfocom.isemid.features.db.utils.DbUtil.validateCredentials;

@RestController
@RequestMapping(ApiPaths.Db.ROOT)
@RequiredArgsConstructor
@Hidden
public class DbController {
    private final DbService dbService;

    @GetMapping("/all")
    public ResponseEntity<List<TableDto>> all(
            @RequestParam String username,
            @RequestParam String password
    ) {
        if (!validateCredentials(username, password)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(dbService.getAllTables());
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeSqlCommand(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(dbService.executeSqlCommand(body));
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> getTableData(
            @PathVariable String tableName,
            @RequestParam String username,
            @RequestParam String password
    ) {
        if (!validateCredentials(username, password)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(dbService.getAllDataFromTable(tableName));
    }

    @GetMapping("/table/{tableName}/delete")
    public ResponseEntity<String> deleteTable(
            @PathVariable String tableName,
            @RequestParam String username,
            @RequestParam String password
    ) {
        if (!validateCredentials(username, password)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Invalid credentials.");
        }
        return ResponseEntity.ok(dbService.deleteTable(tableName));
    }

    @GetMapping("/table/drop-all")
    public ResponseEntity<String> dropAllTables(
            @RequestParam String username,
            @RequestParam String password
    ) {
        if (!validateCredentials(username, password)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Invalid credentials.");
        }
        return ResponseEntity.ok(dbService.dropAllTables());
    }
}
