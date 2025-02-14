package uz.uzinfocom.isemid.repositories.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.card.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Modifying
    @Query("DELETE FROM Card cu WHERE cu.id IN (SELECT c.id FROM Card c WHERE c.form058.id = :formId)")
    void removeForm058References(@Param("formId") Long formId);}
