package uz.uzinfocom.isemid.services;


import uz.uzinfocom.isemid.models.entities.dtos.TableDto;

import java.util.List;
import java.util.Map;

public interface DbService {
    List<TableDto> getAllTables();

    List<Map<String, Object>> getAllDataFromTable(String tableName);

    String deleteTable(String tableName);

    String dropAllTables();
}
