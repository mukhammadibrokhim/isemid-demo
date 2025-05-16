package uz.uzinfocom.isemid.features.form058.reposiotries;

import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.stream.Stream;

public interface Form058RepositoryCustom {
    Stream<Form058> streamAll(Specification<Form058> filteredSpecification);
}
