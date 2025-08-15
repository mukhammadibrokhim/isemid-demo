package uz.uzinfocom.isemid.features.card.reposiotries;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;

import java.util.Optional;

@Repository
public interface CardTubeRepository extends JpaRepository<CardTube, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select c 
                from CardTube c 
                where c.id = :id
                  and c.cardType = :cardType
            """)
    Optional<CardTube> findByIdAndTypeForUpdate(@Param("id") Long id, @Param("cardType") CardType cardType);
}
