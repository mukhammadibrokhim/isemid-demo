package com.abdulatif.emis.repository.organization;

import com.abdulatif.emis.model.organization.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, PagingAndSortingRepository<Organization, Long>, QueryByExampleExecutor<Organization> {

    @Query("select o from Organization o where o.status = 'ACTIVE' and o.uuid = ?1 ")
    Organization findByUuid(String uuid);

    @Query("select o from Organization o where o.status = 'ACTIVE' and o.name = ?1 ")
    Optional<Organization> findByName(String name);

    @Query("select o from Organization o where lower(o.name) like %?1% and o.status = 'ACTIVE' ")
    Page<Organization> findAllBySearch(String value, Pageable pageable);

    @Query("select o from Organization o where o.status = 'ACTIVE' ")
    Page<Organization> findAllOrganizations(Pageable pageable);

    @Query("select d from Organization d where d.parent.id = ?1 and d.status = 'ACTIVE' ")
    List<Organization> findOrganizationChildrenById(Long id);

    @Query("select d from Organization d where d.status = 'ACTIVE' and d.id = ?1")
    Optional<Organization> findActiveById(Long id);

    @Query("select o from Organization o where o.status = 'ACTIVE' and o.parent.uuid = ?1 ")
    List<Organization> findAllChildByUuid(String uuid);
}
