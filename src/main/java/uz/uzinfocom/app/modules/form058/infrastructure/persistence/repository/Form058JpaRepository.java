package uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.util.Optional;

public interface Form058JpaRepository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Form058 f
            WHERE f.id = :id
              AND f.deleteInfo.deleted = false
            """)
    Optional<Form058> findByIdAndDeletedFalse(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Form058 f
            WHERE f.id = :id
              AND f.deleteInfo.deleted = false
            """)
    Optional<Form058> findActiveByIdForUpdate(@Param("id") Long id);

}
