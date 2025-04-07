package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

import java.time.LocalDate;

public interface Form058ReportService {
    ApiResponse<?> reportForm6(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser);

    ApiResponse<?> reportForRegion(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser);
    ApiResponse<?> reportForDistrictByRegionCode(String regionCode,LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser);
}
