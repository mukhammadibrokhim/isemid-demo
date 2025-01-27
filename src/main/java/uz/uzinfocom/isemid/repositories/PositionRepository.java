package uz.uzinfocom.isemid.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
}
