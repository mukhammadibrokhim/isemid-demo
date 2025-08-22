package uz.uzinfocom.isemid.commons.seed;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvLoaderUtil {
    private final ResourceLoader resourceLoader;

    public List<String[]> readCsv(String path) {
        try {
            Resource resource = resourceLoader.getResource(path);
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader);
                 CSVReader csvReader = new CSVReader(bufferedReader)) {
                return csvReader.readAll();
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("CSV reading error: " + path, e);
        }
    }

}
