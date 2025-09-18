package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Address;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationReportHelper {
    private final CatalogService catalogService;

    public LocationReportDto buildRegionReports(List<Form058> forms) {

        List<LocationReportDto.ReportDto> regions = catalogService.finAllByType(CatalogType.state).stream()
                .map(region -> {
                    long confirmed = forms.stream()
                            .filter(f -> f.getStatus() == FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && region.getCode().equals(addr.getStateCode()))
                            .count();

                    long unconfirmed = forms.stream()
                            .filter(f -> f.getStatus() != FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && region.getCode().equals(addr.getStateCode()))
                            .count();


                    return LocationReportDto.ReportDto.builder()
                            .code(region.getCode())
                            .name(region.getNameRu())
                            .confirmed((int) confirmed)
                            .unconfirmed((int) unconfirmed)
                            .children(List.of())
                            .build();
                })
                .toList();

        return getLocationReportDto(forms, regions, forms.size());
    }

    private LocationReportDto getLocationReportDto(List<Form058> addrForms, List<LocationReportDto.ReportDto> regions,
                                                   int size) {
        long confirmed = addrForms.stream()
                .filter(f -> f.getStatus() == FormStatus.APPROVED)
                .count();

        long unconfirmed = addrForms.stream()
                .filter(f -> f.getStatus() != FormStatus.APPROVED)
                .count();
        return LocationReportDto.builder()
                .reports(regions)
                .totalConfirmed((int) confirmed)
                .totalUnconfirmed((int) unconfirmed)
                .totalDocuments(size)
                .build();
    }

    private Address getTemporaryAddress(Form058 form) {
        return form.getPatient().getAddresses()
                .stream()
                .filter(addr -> addr.getType().equals(AddressType.TEMPORARY))
                .findFirst()
                .orElse(null);
    }


    public LocationReportDto buildDistrictReports(String parentCode, List<Form058> forms) {
        List<LocationReportDto.ReportDto> districts = catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode).stream()
                .map(district -> {

                    long confirmed = forms.stream()
                            .filter(f -> f.getStatus() == FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && district.getCode().equals(addr.getCityCode()))
                            .count();

                    long unconfirmed = forms.stream()
                            .filter(f -> f.getStatus() != FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && district.getCode().equals(addr.getCityCode()))
                            .count();


                    return LocationReportDto.ReportDto.builder()
                            .code(district.getCode())
                            .name(district.getNameRu())
                            .confirmed((int) confirmed)
                            .unconfirmed((int) unconfirmed)
                            .children(List.of())
                            .build();
                }).toList();

        long confirmed = forms.stream()
                .filter(f -> f.getStatus() == FormStatus.APPROVED)
                .count();

        long unconfirmed = forms.stream()
                .filter(f -> f.getStatus() != FormStatus.APPROVED)
                .count();
        return LocationReportDto.builder()
                .reports(districts)
                .totalConfirmed((int) confirmed)
                .totalUnconfirmed((int) unconfirmed)
                .totalDocuments(forms.size())
                .build();
    }


    public LocationReportDto buildNeighborhoodReports(String parentCode, List<Form058> forms) {
        List<LocationReportDto.ReportDto> regions = catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode).stream()
                .map(nbh -> {
                    long confirmed = forms.stream()
                            .filter(f -> f.getStatus() == FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && nbh.getCode().equals(addr.getNeighborhoodCode()))
                            .count();

                    long unconfirmed = forms.stream()
                            .filter(f -> f.getStatus() != FormStatus.APPROVED)
                            .map(this::getTemporaryAddress)
                            .filter(addr -> addr != null && nbh.getCode().equals(addr.getNeighborhoodCode()))
                            .count();


                    return LocationReportDto.ReportDto.builder()
                            .code(nbh.getCode())
                            .name(nbh.getNameRu())
                            .confirmed((int) confirmed)
                            .unconfirmed((int) unconfirmed)
                            .children(List.of())
                            .build();
                }).toList();

        return getLocationReportDto(forms, regions, forms.size());
    }

}