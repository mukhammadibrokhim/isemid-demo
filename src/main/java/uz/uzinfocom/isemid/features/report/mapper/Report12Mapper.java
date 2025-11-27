package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.report.dtos.Report12Dto;
import uz.uzinfocom.isemid.features.report.models.Report12;

@Mapper(componentModel = "spring")
public interface Report12Mapper {
    Report12 toEntity(Report12Dto report12Dto);
    Report12Dto toDto(Report12 report12);
}
