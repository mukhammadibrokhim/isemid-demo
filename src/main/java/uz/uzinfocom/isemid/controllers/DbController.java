package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.dtos.TableDto;
import uz.uzinfocom.isemid.services.DbService;

import java.util.List;
import java.util.Map;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.DB;
import static uz.uzinfocom.isemid.utils.CommonUtils.validateCredentials;


@RestController
@RequestMapping(DB)
@RequiredArgsConstructor
@Hidden
public class DbController {

    final DbService dbService;

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
