package com.abdulatif.emis.controller.region;

import com.abdulatif.emis.model.util.Pagination;
import com.abdulatif.emis.model.util.Search;
import com.abdulatif.emis.service.region.DistrictService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/district")
@SecurityRequirement(name = "Bearer Authentication")
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> get(
            @PathVariable Long id
    ) {
        return districtService.get(id);
    }

    @PostMapping("/list-search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listSearch(
            @RequestBody Pagination<Search> pagination
    ) {
        return districtService.listSearch(pagination);
    }

    @GetMapping("/get-by-region/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getByRegion(
            @PathVariable Long id
    ) {
        return districtService.getByRegion(id);
    }

}
