package uz.uzinfocom.isemid.features.db.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.db.dtos.TableDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DbService {


    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String executeSqlCommand(String sql) {
        try {
            String[] queries = sql.split(";");
            for (String q : queries) {
                if (!q.trim().isEmpty()) {
                    entityManager.createNativeQuery(q.trim()).executeUpdate();
                }
            }
            return "SQL command(s) executed successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing SQL command: " + e.getMessage();
        }
    }

    public List<TableDto> getAllTables() {
        String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        Query nativeQuery = entityManager.createNativeQuery(query);
        return (List<TableDto>) nativeQuery.getResultList();
    }

    public List<Map<String, Object>> getAllDataFromTable(String tableName) {
        String query = "SELECT * FROM " + tableName;
        Query nativeQuery = entityManager.createNativeQuery(query);

        List<Object[]> resultList = nativeQuery.getResultList();
        List<String> columnNames = getColumnNames(tableName);

        List<Map<String, Object>> dataWithColumnNames = new ArrayList<>();

        for (Object[] row : resultList) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < row.length; i++) {
                rowMap.put(columnNames.get(i), row[i]);
            }
            dataWithColumnNames.add(rowMap);
        }

        return dataWithColumnNames;
    }

    @Transactional
    public String deleteTable(String tableName) {
        try {
            if (tableName == null || tableName.isEmpty()) {
                return "Error: Table name is empty.";
            }

            if (tableName.equals("information_schema") || tableName.equals("pg_catalog")) {
                return "Error: Cannot delete system table '" + tableName + "'.";
            }
            String query = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
            entityManager.createNativeQuery(query).executeUpdate();

            return "Table '" + tableName + "' has been successfully deleted.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to delete table '" + tableName + "'. " + e.getMessage();
        }
    }


    @Transactional
    public String dropAllTables() {
        try {
            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
            Query nativeQuery = entityManager.createNativeQuery(query);
            List<String> tableNames = nativeQuery.getResultList();

            for (String tableName : tableNames) {
                if (tableName.equals("information_schema") || tableName.equals("pg_catalog")) {
                    continue;
                }

                String dropQuery = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
                entityManager.createNativeQuery(dropQuery).executeUpdate();
            }

            return "All tables have been successfully deleted.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to delete tables. " + e.getMessage();
        }
    }
    private List<String> getColumnNames(String tableName) {
        String query = "SELECT column_name FROM information_schema.columns WHERE table_name = :tableName";
        Query nativeQuery = entityManager.createNativeQuery(query);
        nativeQuery.setParameter("tableName", tableName);

        List<String> columnNames = nativeQuery.getResultList();
        return columnNames;
    }
}
