package uz.uzinfocom.isemid.features.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.isemid.features.security.model.TokenRevocation;

import java.time.Instant;

public interface TokenRevocationRepository extends JpaRepository<TokenRevocation, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from TokenRevocation r where r.expiresAt <= :now")
    void deleteExpired(@Param("now") Instant now);
}
