package uz.uzinfocom.isemid.features.form0581.reposiotries;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;

import java.util.List;

@Repository
public interface Form0581Repository extends JpaRepository<Form0581, Long>, JpaSpecificationExecutor<Form0581> {

    @EntityGraph(attributePaths = {"patient.addresses"})
    List<Form0581> findAll(Specification<Form0581> spec);
}
