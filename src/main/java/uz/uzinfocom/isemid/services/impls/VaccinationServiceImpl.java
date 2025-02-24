package uz.uzinfocom.isemid.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.dtos.VaccinationDto;
import uz.uzinfocom.isemid.models.payload.responses.RespVaccination;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.VaccinationService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.VACCINATION_NNUZB;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class VaccinationServiceImpl implements VaccinationService {
    @SneakyThrows
    @Override
    public ApiResponse<?> getVaccinationByNnuzbLast(String nnuzb) {
        String resp = makeRequest(String.format(VACCINATION_NNUZB, nnuzb), HttpMethod.GET, null, null, String.class);
        return new ApiResponse<>(true, "", mapping(resp));
    }

    @SneakyThrows
    private List<RespVaccination> mapping(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Tashkent"));

        VaccinationDto dto = mapper.readValue(jsonString, VaccinationDto.class);
        if (dto.getResult() != null) {
            List<VaccinationDto.Data> data = dto.getResult();

            Map<String, VaccinationDto.Data> latestVaccinations = new HashMap<>();

            for (VaccinationDto.Data entry : data) {
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
                        return RespVaccination.builder()
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
