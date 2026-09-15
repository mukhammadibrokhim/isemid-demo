package uz.uzinfocom.app.modules.reference.application.population.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.modules.reference.application.population.sync.dto.SdmxPopulationDataset;

/**
 * Fetches and parses the public stat.uz SDMX population export. The body is
 * a one-element JSON array holding the dataset object.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopulationSdmxClient {

    private final RestClient restClient;
    private final PopulationSyncProperties properties;

    public SdmxPopulationDataset fetch() {
        String url = properties.getSdmxUrl();
        log.info("Fetching population SDMX feed from {}", url);

        SdmxPopulationDataset[] body;
        try {
            body = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL)
                    .retrieve()
                    .body(SdmxPopulationDataset[].class);
        } catch (RuntimeException exception) {
            throw new PopulationSyncException("reference.population.sync.fetch_failed", exception);
        }

        if (body == null || body.length == 0 || body[0] == null) {
            throw new PopulationSyncException("reference.population.sync.empty_response");
        }

        SdmxPopulationDataset dataset = body[0];
        if (dataset.rows().isEmpty()) {
            throw new PopulationSyncException("reference.population.sync.empty_response");
        }

        return dataset;
    }
}
