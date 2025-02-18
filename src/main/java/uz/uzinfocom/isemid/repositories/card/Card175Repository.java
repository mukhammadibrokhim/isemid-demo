package uz.uzinfocom.isemid.repositories.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.card.card175.Card175;

@Repository
public interface Card175Repository extends JpaRepository<Card175, Long>, JpaSpecificationExecutor<Card175> {
}
