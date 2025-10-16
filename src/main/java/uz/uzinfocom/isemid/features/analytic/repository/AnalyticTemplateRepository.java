package uz.uzinfocom.isemid.features.analytic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticTemplate;

import java.util.List;

public interface AnalyticTemplateRepository extends JpaRepository<AnalyticTemplate, Long> {
    List<AnalyticTemplate> findAllByOwnerIdOrGlobalTrue(Long ownerId);

}
