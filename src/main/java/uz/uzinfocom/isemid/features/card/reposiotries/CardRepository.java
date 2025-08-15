package uz.uzinfocom.isemid.features.card.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    @Query(
            value = """
                    SELECT * 
                    FROM card c
                    WHERE c.id = :id 
                      AND c.card_type = :cardType
                    """,
            nativeQuery = true
    )
    Optional<Card> findByIdAndType(@Param("id") Long id, @Param("cardType") String cardType);

}
