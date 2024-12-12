package com.abdulatif.emis.repository.user;


import com.abdulatif.emis.dto.user.UserResponseDTO;
import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, PagingAndSortingRepository<User, Long>, QueryByExampleExecutor<User> {
    User findByUsername(String name);

    @Query("select new com.abdulatif.emis.dto.user.UserResponseDTO(u) from User u where u.status = 'ACTIVE' and ?2 member of u.parentOrganizations " +
            "and (lower(u.firstName) like %?1% or lower(u.lastName) like %?1% or lower(u.middleName) like %?1% or lower(u.username) like %?1%)")
    Page<UserResponseDTO>findAllBySearch(String value, Organization organization, Pageable pageable);

    @Query("select new com.abdulatif.emis.dto.user.UserResponseDTO(u) from User u where u.status = 'ACTIVE' and ?1 member of u.parentOrganizations ")
    Page<UserResponseDTO> findAllUsers(Organization organization, Pageable pageable);

    @Query("select d from User d where d.status = 'ACTIVE' and d.id = ?1")
    Optional<User> findActiveById(Long id);
}
