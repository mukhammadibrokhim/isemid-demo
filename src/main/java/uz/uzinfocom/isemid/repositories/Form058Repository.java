package uz.uzinfocom.isemid.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Form058;

@Repository
public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058> {
}
