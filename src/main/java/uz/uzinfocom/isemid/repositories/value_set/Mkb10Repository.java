package uz.uzinfocom.isemid.repositories.value_set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;

import java.util.List;
import java.util.Map;


@Repository
public interface Mkb10Repository extends JpaRepository<Mkb10, Long>, JpaSpecificationExecutor<Mkb10> {
    List<Mkb10> findByParentIdIsNull();

    List<Mkb10> findAllByCode(String code);
}
