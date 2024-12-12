package com.abdulatif.emis.service.region;

import com.abdulatif.emis.dto.region.DistrictDTO;
import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.util.*;
import com.abdulatif.emis.repository.region.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;

    public ResponseEntity<?> get(Long id) {
        District district = districtRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException("District with id " + id + " not found")
        );
        return ResponseEntity.ok(
                new DistrictDTO(district)
        );
    }

    public ResponseEntity<?> listSearch(Pagination<Search> pagination) {
        Page<District> districtPage;
        if (pagination.getSearch().getValue() != null && !pagination.getSearch().getValue().isEmpty()) {
            districtPage = search(pagination.getSearch().getValue().toLowerCase(), pagination);
        } else {
            districtPage = search("", pagination);
        }
        List<DistrictDTO> districtDTOS = districtPage.getContent().stream().map(DistrictDTO::new).toList();
        return ResponseEntity.ok(new ResponseDto<>(ResponseDto.States.SUCCESS, HttpStatus.OK, 200, null, PageWrapper.builder().list(districtDTOS).total(districtPage.getTotalElements()).build()));
    }

    public Page<District> search(String value, Pagination<Search> pagination) {
        Pageable paging = Utils.getPageableFromPagination(pagination);
        Page<District> pagedResult;
        if (!value.isEmpty()) {
            pagedResult = districtRepository.findAllBySearch(value, paging);
        } else {
            pagedResult = districtRepository.findAllDistricts(paging);
        }
        return pagedResult;
    }

    public ResponseEntity<?> getByRegion(Long id) {
        return ResponseEntity.ok(
                districtRepository.findAllByRegionId(id)
        );
    }
}
