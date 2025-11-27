package uz.uzinfocom.isemid.features.card.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Query("""
       SELECT c FROM Card c
       WHERE c.id = :id\s
         AND c.cardType = :cardType
      \s""")
    Optional<Card> findByIdAndType(@Param("id") Long id, @Param("cardType") CardType cardType);

    @Query("select c.form058 from Card c where c.id = :cardId")
    Optional<Form058> findForm058ByCardId(Long cardId);

}
