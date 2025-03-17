package uz.uzinfocom.isemid.repositories.act;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.card.Card;

@Repository
public interface ActRepository extends JpaRepository<Act, Long>, JpaSpecificationExecutor<Act> {
}
