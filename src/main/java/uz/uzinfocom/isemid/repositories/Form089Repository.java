package uz.uzinfocom.isemid.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Form089;

@Repository
public interface Form089Repository extends JpaRepository<Form089, Long> {
}
