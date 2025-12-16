package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.Report12Dto;
import uz.uzinfocom.isemid.features.report.model.FormReport12;
import uz.uzinfocom.isemid.features.user.models.User;

public interface FormReport12Service {
    FormReport12 create(Report12Dto report12Dto);

    FormReport12 update(Report12Dto report12Dto);

    void delete(Long id);

    Page<FormReport12> filter(int page, int size, String regionName, User user);

}
