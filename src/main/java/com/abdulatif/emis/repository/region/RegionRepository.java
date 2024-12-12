package com.abdulatif.emis.repository.region;

import com.abdulatif.emis.model.region.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long>, PagingAndSortingRepository<Region, Long>, QueryByExampleExecutor<Region> {

    @Query("select r from Region r " +
            "where lower(r.name) like %?1% order by r.name ")
    Page<Region> findAllBySearch(String value, Pageable pageable);

    @Query("select r from Region r order by r.name ")
    Page<Region> findAllRegions(Pageable pageable);

    Optional<Region> findByName(String name);

}
