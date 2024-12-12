package com.abdulatif.emis.service.region;

import com.abdulatif.emis.dto.region.RegionDTO;
import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.region.Region;
import com.abdulatif.emis.model.util.*;
import com.abdulatif.emis.repository.region.DistrictRepository;
import com.abdulatif.emis.repository.region.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;

    public ResponseEntity<?> get(Long id) {
        Region region = regionRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException("Region not found with id " + id)
        );
        RegionDTO regionDTO = new RegionDTO(region);
        regionDTO.setDistricts(districtRepository.findAllByRegionId(id));
        return ResponseEntity.ok(regionDTO);
    }

    public ResponseEntity<?> listSearch(Pagination<Search> pagination) {
        Page<Region> regionPage;
        if (pagination.getSearch().getValue() != null && !pagination.getSearch().getValue().isEmpty()) {
            regionPage = search(pagination.getSearch().getValue().toLowerCase(), pagination);
        } else {
            regionPage = search("", pagination);
        }
        List<RegionDTO> regionDTOs = regionPage.getContent().stream().map(RegionDTO::new).toList();
        return ResponseEntity.ok(new ResponseDto<>(ResponseDto.States.SUCCESS, HttpStatus.OK, 200, null, PageWrapper.builder().list(regionDTOs).total(regionPage.getTotalElements()).build()));
    }

    public Page<Region> search(String value, Pagination<Search> pagination) {
        Pageable paging = Utils.getPageableFromPagination(pagination);
        Page<Region> pagedResult;
        if (!value.isEmpty()) {
            pagedResult = regionRepository.findAllBySearch(value, paging);
        } else {
            pagedResult = regionRepository.findAllRegions(paging);
        }
        return pagedResult;
    }
}
