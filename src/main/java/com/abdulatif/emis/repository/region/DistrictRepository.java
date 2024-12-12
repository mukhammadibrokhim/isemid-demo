package com.abdulatif.emis.repository.region;

import com.abdulatif.emis.model.region.District;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long>, PagingAndSortingRepository<District, Long>, QueryByExampleExecutor<District> {

    @Query("select d from District d where d.region.id = ?1 ")
    List<District> findAllByRegionId(Long id);

    @Query("select r from District r " +
            "where lower(r.name) like %?1% order by r.name ")
    Page<District> findAllBySearch(String value, Pageable pageable);

    @Query("select r from District r ")
    Page<District> findAllDistricts(Pageable pageable);

    @Query("select d from District d where d.name = ?1 ")
    District findByName(String name);
}
