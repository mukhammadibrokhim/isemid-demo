package uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;

import java.util.Optional;

public interface Form129JpaRepository extends JpaRepository<Form129, Long>, JpaSpecificationExecutor<Form129> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Form129 f WHERE f.id = :id")
    Optional<Form129> findByIdForUpdate(@Param("id") Long id);
}
