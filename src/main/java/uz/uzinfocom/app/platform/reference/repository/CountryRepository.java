package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.reference.domain.Country;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByIdAndDeletedFalse(Long id);

    Optional<Country> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Country> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();
}
