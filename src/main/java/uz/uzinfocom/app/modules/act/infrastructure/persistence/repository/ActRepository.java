package uz.uzinfocom.app.modules.act.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.act.domain.model.Act;

import java.util.Optional;

public interface ActRepository extends JpaRepository<Act, Long>, JpaSpecificationExecutor<Act> {

    @Query("""
            SELECT a
            FROM Act a
            WHERE a.id = :id
              AND a.deleteInfo.deleted = false
            """)
    Optional<Act> findByIdAndDeletedFalse(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Act a
            WHERE a.id = :id
              AND a.deleteInfo.deleted = false
            """)
    Optional<Act> findActiveByIdForUpdate(@Param("id") Long id);

}
