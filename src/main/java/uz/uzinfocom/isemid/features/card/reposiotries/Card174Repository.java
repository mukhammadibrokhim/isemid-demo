package uz.uzinfocom.isemid.features.card.reposiotries;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;

import java.util.Optional;

@Repository
public interface Card174Repository extends JpaRepository<Card174, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                 select c\s
                 from Card174 c\s
                 where c.id = :id
                   and c.cardType = :cardType
            \s""")
    Optional<Card174> findByIdAndTypeForUpdate(@Param("id") Long id, @Param("cardType") CardType cardType);
}
