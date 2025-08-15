package uz.uzinfocom.isemid.features.card.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.card.models.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

//    // Card161
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select c from Card161 c where c.id = :id")
//    Optional<Card161> findByIdForUpdate(@Param("id") Long id);
//
//    // Card174
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select c from Card174 c where c.id = :id")
//    Optional<Card174> findByIdForUpdate(@Param("id") Long id);
//
//    // Card175
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select c from Card175 c where c.id = :id")
//    Optional<Card175> findByIdForUpdate(@Param("id") Long id);
//
//    // Card205
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select c from Card205 c where c.id = :id")
//    Optional<Card205> findByIdForUpdate(@Param("id") Long id);
//
//    // CardTube
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select c from CardTube c where c.id = :id")
//    Optional<CardTube> findByIdForUpdate(@Param("id") Long id);

}
