package uz.uzinfocom.isemid.features.form058.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.List;

@Repository
public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058>, Form058RepositoryCustom {

    @Query("SELECT f FROM Form058 f " +
            "JOIN f.patient p " +
            "JOIN p.identifiers i " +
            "WHERE i.typeCode = 'NNUZB' AND i.value = :nnuzb")
    List<Form058> findAllByNnuzbIdentifier(@Param("nnuzb") String nnuzb);
}
