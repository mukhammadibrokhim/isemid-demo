package com.abdulatif.emis.controller.region;


import com.abdulatif.emis.model.util.Pagination;
import com.abdulatif.emis.model.util.Search;
import com.abdulatif.emis.service.region.RegionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/regions")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> get(
            @PathVariable Long id
    ) {
        return regionService.get(id);
    }

    @PostMapping("/list-search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listSearch(
            @RequestBody Pagination<Search> pagination
    ) {
        return regionService.listSearch(pagination);
    }

}
