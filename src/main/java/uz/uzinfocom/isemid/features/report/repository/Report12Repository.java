package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.report.model.Report12;

@Repository
public interface Report12Repository extends JpaRepository<Report12, Long>, JpaSpecificationExecutor<Report12> {
}
