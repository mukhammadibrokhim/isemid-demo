package uz.uzinfocom.isemid.features.report.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.report.models.Report12;

@Repository
public interface Report12Repository extends JpaRepository<Report12, Long>, JpaSpecificationExecutor<Report12> {
}
