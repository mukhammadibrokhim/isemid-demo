package uz.uzinfocom.isemid.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Journal060;

@Repository
public interface Journal060Repository extends JpaRepository<Journal060, Long>, JpaSpecificationExecutor<Journal060> {
}
