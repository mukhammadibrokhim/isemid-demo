package uz.uzinfocom.isemid.features.integrations.vacciantion.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.integrations.vacciantion.dtos.response.VaccinationApiResponse;
import uz.uzinfocom.isemid.features.integrations.vacciantion.dtos.response.VaccinationResponse;
import uz.uzinfocom.isemid.features.integrations.vacciantion.services.VaccinationService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;
import static uz.uzinfocom.isemid.features.integrations.gcp.util.ValidationUtil.validateByNnuzb;

@Service
@RequiredArgsConstructor
public class VaccinationServiceImpl implements VaccinationService {
    @Override
    public ApiResponse<?> getVaccinationByNnuzbLast(String nnuzb) {
        validateByNnuzb(nnuzb);
        String resp = makeRequest(String.format(ApiPaths.ExternalApi.VACCINATION_NNUZB, nnuzb), HttpMethod.GET, null, null, String.class);
        return new ApiResponse<>(true, "", mapping(resp));
    }

    @SneakyThrows
    private List<VaccinationResponse> mapping(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Tashkent"));

        VaccinationApiResponse dto = mapper.readValue(jsonString, VaccinationApiResponse.class);
        if (dto.getResult() != null) {
            List<VaccinationApiResponse.Data> data = dto.getResult();

            Map<String, VaccinationApiResponse.Data> latestVaccinations = new HashMap<>();

            for (VaccinationApiResponse.Data entry : data) {
                String vaccineName = entry.getSku().getName();
                if (!latestVaccinations.containsKey(vaccineName) ||
                        latestVaccinations.get(vaccineName).getVaccinationDate().isBefore(entry.getVaccinationDate())) {
                    latestVaccinations.put(vaccineName, entry);
                }
            }

            return latestVaccinations.values().stream()
                    .map(data1 -> {
                        ZonedDateTime tashkentTime = data1.getVaccinationDate()
                                .atZoneSameInstant(ZoneId.of("Asia/Tashkent"));
                        return VaccinationResponse.builder()
                                .id(data1.getId())
                                .firstName(data1.getFirstName())
                                .lastName(data1.getLastName())
                                .middleName(data1.getMiddleName())
                                .birthDate(data1.getBirthDate())
                                .mpi(data1.getMpi())
                                .vaccinationName(data1.getSku().getName())
                                .vaccinationDate(tashkentTime.toLocalDateTime()) // Eng oxirgi sanani tanlash
                                .unitInDose(data1.getSku().getUnitInDose())
                                .doseVolume(data1.getSku().getDoseVolume())
                                .serialNumber(data1.getSku().getSerialNumber())
                                .vaccinationExpire(data1.getVaccinationExpires())
                                .build();
                    }).toList();
        }
        return Collections.emptyList();
    }

}
