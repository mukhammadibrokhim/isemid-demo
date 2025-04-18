package uz.uzinfocom.isemid.features.act.repositores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.act.models.Act;

@Repository
public interface ActRepository extends JpaRepository<Act, Long>, JpaSpecificationExecutor<Act> {
}
