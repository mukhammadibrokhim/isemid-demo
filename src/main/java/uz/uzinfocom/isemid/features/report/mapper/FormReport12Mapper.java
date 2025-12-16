package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.report.dtos.Report12Dto;
import uz.uzinfocom.isemid.features.report.model.FormReport12;

@Mapper(componentModel = "spring")
public interface FormReport12Mapper {
    FormReport12 toEntity(Report12Dto report12Dto);

    Report12Dto toDto(FormReport12 report12);
}
