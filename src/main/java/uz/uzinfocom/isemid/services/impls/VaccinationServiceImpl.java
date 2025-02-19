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
import java.util.Comparator;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.VACCINATION_NNUZB;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class VaccinationServiceImpl implements VaccinationService {
    @SneakyThrows
    @Override
    public ApiResponse<?> getVaccinationByNnuzbLast(String nnuzb) {
        String resp = makeRequest(String.format(VACCINATION_NNUZB, nnuzb), HttpMethod.GET, null, null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        VaccinationDto dto = mapper.readValue(resp, VaccinationDto.class);
        if (dto != null && dto.getResult() == null) {
            return new ApiResponse<>(false, "", null);
        }
        return new ApiResponse<>(true, "", dto);
    }

    @SneakyThrows
    private RespVaccination mapping(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        VaccinationDto dto = mapper.readValue(jsonString, VaccinationDto.class);
        if (dto.getResult() != null) {

            VaccinationDto.Data data = dto.getResult().stream()
                    .max(Comparator.comparing(VaccinationDto.Data::getVaccinationDate))
                    .orElse(null);
            if (data != null) {
                ZonedDateTime tashkentTime = data.getVaccinationDate()
                        .atZoneSameInstant(ZoneId.of("Asia/Tashkent"));

                return RespVaccination.builder()
                        .vaccinationName(data.getSku().getName())
                        .vaccinationDate(tashkentTime.toLocalDateTime())
                        .unitInDose(data.getSku().getUnitInDose())
                        .doseVolume(data.getSku().getDoseVolume())
                        .serialNumber(data.getSku().getSerialNumber())
                        .build();
            }
        }
        return null;
    }
}
