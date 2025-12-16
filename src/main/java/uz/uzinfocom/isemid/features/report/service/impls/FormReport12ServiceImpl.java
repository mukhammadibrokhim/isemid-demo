package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.Report12Dto;
import uz.uzinfocom.isemid.features.report.mapper.FormReport12Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport12;
import uz.uzinfocom.isemid.features.report.repository.FormReport12Repository;
import uz.uzinfocom.isemid.features.report.service.FormReport12Service;
import uz.uzinfocom.isemid.features.report.specification.FormReport12Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;


@Service
@RequiredArgsConstructor
public class FormReport12ServiceImpl implements FormReport12Service {

    private final FormReport12Repository report12Repository;
    private final FormReport12Mapper report12Mapper;

    @Override
    public FormReport12 create(Report12Dto report12Dto) {
        FormReport12 entity = report12Mapper.toEntity(report12Dto);
        return report12Repository.save(entity);
    }

    @Override
    public FormReport12 update(Report12Dto report12Dto) {

        report12Repository.findById(report12Dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Report12 not found with id " + report12Dto.getId()));

        FormReport12 updated = report12Mapper.toEntity(report12Dto);

        return report12Repository.save(updated);
    }

    @Override
    public void delete(Long id) {
        if (!report12Repository.existsById(id)) {
            throw new EntityNotFoundException("Report12 not found with id " + id);
        }
        report12Repository.deleteById(id);
    }

    @Override
    public Page<FormReport12> filter(int page, int size, String regionName, User user) {
        Specification<FormReport12> spec = FormReport12Specification.build(regionName, user);
        return report12Repository.findAll(spec, pageableSortByIdDesc(page, size));
    }
}
