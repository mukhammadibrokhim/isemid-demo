package uz.uzinfocom.app.platform.iam.application.organization.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.*;
import uz.uzinfocom.app.platform.iam.application.organization.query.mapper.OrganizationQueryMapper;
import uz.uzinfocom.app.platform.iam.application.organization.query.projection.OrganizationTableProjection;
import uz.uzinfocom.app.platform.iam.application.organization.query.specification.OrganizationSpecification;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationQueryService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationQueryMapper organizationQueryMapper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<OrganizationTableResponse> findTable(OrganizationFilerRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                OrganizationSortFields.ALLOWED_SORT_FIELDS
        );

        Page<OrganizationTableProjection> page = Objects.requireNonNull(organizationRepository.findBy(
                OrganizationSpecification.byFilter(request),
                query -> query
                        .as(OrganizationTableProjection.class)
                        .page(pageable)
        ), "Organization table page return null");

        return page.map(organizationQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public OrganizationDetailResponse findDetail(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("organization.not_found"));

        return organizationQueryMapper.toDetailedResponse(organization, auditResolver.resolve(organization));
    }

    @Transactional(readOnly = true)
    public Page<OrganizationUserLookupResponse> findUserLookupsByOrganizationId(
            Long organizationId,
            OrganizationUserLookupRequest request
    ) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new NotFoundException("organization.not_found");
        }

        Pageable pageable = PageableUtils.of(
                request,
                OrganizationUserLookupSortFields.DEFAULT_SORT_BY,
                OrganizationUserLookupSortFields.DEFAULT_SORT_DIRECTION,
                OrganizationUserLookupSortFields.ALLOWED_SORT_FIELDS
        );

        return userRepository.findUserLookupsByOrganizationId(
                organizationId,
                request.normalizedSearch(),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<OrganizationLookupResponse> lookup(OrganizationLookupRequest request) {
        Pageable pageable = PageRequest.of(0, request.normalizedLimit());

        return organizationRepository.lookupOrganizations(
                request.normalizedSearch(),
                request.levelType(),
                request.medicalType(),
                request.active(),
                pageable
        );
    }
}
