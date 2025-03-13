package uz.uzinfocom.isemid.repositories.act.act154;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;

public interface Act154Repository extends JpaRepository<Act154, Long>, JpaSpecificationExecutor<Act154> {
}
