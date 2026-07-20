package uz.uzinfocom.isemid.features.form129.reposiotries;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form129.models.Form129;

import java.util.List;

@Repository
public interface Form129Repository extends JpaRepository<Form129, Long>, JpaSpecificationExecutor<Form129> {

    @EntityGraph(attributePaths = {"patient.addresses"})
    List<Form129> findAll(Specification<Form129> spec);
}
