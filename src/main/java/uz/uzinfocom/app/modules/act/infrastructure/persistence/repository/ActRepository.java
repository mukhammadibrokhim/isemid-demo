package uz.uzinfocom.app.modules.act.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.act.domain.model.Act;

public interface ActRepository extends JpaRepository<Act, Long>, JpaSpecificationExecutor<Act> {

}
