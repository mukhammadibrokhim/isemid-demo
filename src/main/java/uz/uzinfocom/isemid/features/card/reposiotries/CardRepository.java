package uz.uzinfocom.isemid.features.card.reposiotries;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                 select c\s
                 from Card c\s
                 where c.id = :id\s
                   and c.cardType = :cardType
            \s""")
    Optional<Card> findByIdAndTypeForUpdate(@Param("id") Long id, @Param("cardType") CardType cardType);


}
