package com.abdulatif.emis.service.system;

import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.model.region.District;
import com.abdulatif.emis.model.region.Region;
import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.repository.region.DistrictRepository;
import com.abdulatif.emis.repository.region.RegionRepository;
import com.abdulatif.emis.repository.user.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class InitialData {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionsRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final RoleRepository roleRepository;


    @PostConstruct
    public void init() throws IOException {
//        loadRegions();
//        loadDistricts();
//        loadRole();
    }

    private void loadDistricts() throws IOException {
//        districtRepository.deleteAll();
        Resource resource = resourceLoader.getResource("classpath:districts.json");
        Map mapLoinc = objectMapper.readValue(resource.getInputStream(), Map.class);
        List<Map> districtMapList = (ArrayList<Map>) mapLoinc.get("districts");
        districtMapList.parallelStream().forEach(map1 -> {
            String name = String.valueOf(map1.get("name"));
            String regionName = String.valueOf(map1.get("region_name"));
            regionsRepository.findByName(regionName)
                    .ifPresent((region) ->
                            districtRepository.save(
                                    District.builder()
                                            .name(name)
                                            .region(region)
                                            .build())
                    );
        });
    }

    private void loadRegions() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Resource resource = resourceLoader.getResource("classpath:regions.json");
        Map mapLoinc = objectMapper.readValue(resource.getInputStream(), Map.class);
        List<Map> regionMapList = (ArrayList<Map>) mapLoinc.get("regions");
        executor.submit(() -> {
            regionMapList.parallelStream().forEach(map1 -> {
                String name = String.valueOf(map1.get("name"));
                Long id = Long.valueOf(map1.get("id").toString());
                if (regionsRepository.findByName(name).isEmpty()) {
                    regionsRepository.save(
                            Region.builder()
                                    .id(id)
                                    .name(name)
                                    .build()
                    );
                }
            });
        });
        executor.shutdown();
    }

    public void loadRole() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Resource resource = resourceLoader.getResource("classpath:roles.json");
        Map mapLoinc = objectMapper.readValue(resource.getInputStream(), Map.class);
        List<Map> roleMapList = (ArrayList<Map>) mapLoinc.get("roles");
        executor.submit(() -> {
            roleMapList.parallelStream().forEach(map1 -> {
                String name = String.valueOf(map1.get("name"));
                if (roleRepository.findByName(name) == null) {
                    String code = String.valueOf(map1.get("code"));
                    roleRepository.save(
                            Role.builder()
                                    .name(name)
                                    .code(code)
                                    .status(Status.ACTIVE)
                                    .build()
                    );
                }
            });
        });
        executor.shutdown();
    }
}

