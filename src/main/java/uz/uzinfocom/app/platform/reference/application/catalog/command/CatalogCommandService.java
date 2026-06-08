package uz.uzinfocom.app.platform.reference.application.catalog.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogCreateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.mapper.CatalogMapper;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.domain.Catalog;
import uz.uzinfocom.app.platform.reference.repository.CatalogRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogCommandService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;

    @Transactional
    public CatalogResponse create(CatalogCreateRequest request) {
        String type = requireType(request.type());
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = normalizeOptionalParentCode(request.parentCode());

        if (catalogRepository.existsByTypeAndCode(type, code)) {
            throw new ConflictException("reference.catalog.code.already_exists", type, code);
        }

        Catalog catalog = Catalog.builder()
                .type(type)
                .code(code)
                .parentCode(parentCode)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .build();

        Catalog saved = catalogRepository.save(catalog);
        log.debug("Reference catalog created. id={}, type={}, code={}", saved.getId(), saved.getType(), saved.getCode());

        return catalogMapper.toResponse(saved);
    }

    @Transactional
    public CatalogResponse update(Long id, CatalogUpdateRequest request) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.catalog.not_found_by_id", id));

        if (catalog.isDeleted()) {
            throw new ConflictException("reference.catalog.update.deleted_conflict", id);
        }

        String type = requireType(request.type());
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = normalizeOptionalParentCode(request.parentCode());

        if ((!Objects.equals(catalog.getType(), type) || !Objects.equals(catalog.getCode(), code))
                && catalogRepository.existsByTypeAndCode(type, code)) {
            throw new ConflictException("reference.catalog.code.already_exists", type, code);
        }

        catalog.setType(type);
        catalog.setCode(code);
        catalog.setParentCode(parentCode);
        catalog.setNameUz(request.nameUz());
        catalog.setNameUzCyril(request.nameUzCyril());
        catalog.setNameRu(request.nameRu());
        catalog.setNameKaa(request.nameKaa());

        Catalog saved = catalogRepository.save(catalog);
        log.debug("Reference catalog updated. id={}, type={}, code={}", saved.getId(), saved.getType(), saved.getCode());

        return catalogMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.catalog.not_found_by_id", id));

        if (catalog.isDeleted()) {
            return;
        }

        catalog.setDeleted(true);
        catalogRepository.save(catalog);

        log.debug("Reference catalog soft-deleted. id={}, type={}, code={}",
                catalog.getId(), catalog.getType(), catalog.getCode());
    }

    private String requireType(String type) {
        if (type == null) {
            throw new ConflictException("reference.catalog.type.required");
        }
        return type;
    }

    private String normalizeOptionalParentCode(String parentCode) {
        if (!StringUtils.hasText(parentCode)) {
            return null;
        }
        return parentCode.trim().toUpperCase(Locale.ROOT);
    }

}
