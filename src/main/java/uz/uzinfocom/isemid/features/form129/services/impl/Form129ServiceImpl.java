package uz.uzinfocom.isemid.features.form129.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.security.scope.OrganizationAccessScopeResolver;
import uz.uzinfocom.isemid.commons.security.scope.ResolvedAccessScope;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129FilterRequest;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129Request;
import uz.uzinfocom.isemid.features.form129.mappers.Form129Mapper;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.form129.reposiotries.Form129Repository;
import uz.uzinfocom.isemid.features.form129.services.Form129Service;
import uz.uzinfocom.isemid.features.form129.specifications.Form129AccessSpecification;
import uz.uzinfocom.isemid.features.form129.specifications.Form129Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.User;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.features.form129.util.Form129BindingUtil.bindChildEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class Form129ServiceImpl implements Form129Service {

    private final Form129Repository form129Repository;
    private final Form129Mapper form129Mapper;
    private final MessageUtil messageUtil;
    private final Form129AccessSpecification form129AccessSpecification;
    private final OrganizationAccessScopeResolver organizationAccessScopeResolver;

    @Override
    @Transactional
    public Form129 create(Form129Request request, User currentUser) {
        Form129 form129 = form129Mapper.toEntity(request);

        bindChildEntity(form129);

        Form129 saved = form129Repository.save(form129);

        log.info("form129.create.success formId={} actorUserId={}", saved.getId(), currentUser.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form129> getAll(Form129FilterRequest filterRequest, User user, FilterType filterType) {
        Organization currentOrganization = user.getCurrentOrganization();
        ResolvedAccessScope scope = organizationAccessScopeResolver.resolve(currentOrganization);

        FilterType effectiveFilterType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form129> specification = Specification
                .where(Form129Specification.build(filterRequest, effectiveFilterType))
                .and(form129AccessSpecification.byScope(
                        effectiveFilterType,
                        scope,
                        currentOrganization == null ? null : currentOrganization.getId(),
                        filterRequest == null ? null : filterRequest.affiliation()
                ));

        assert filterRequest != null;
        return form129Repository.findAll(
                specification,
                PageRequest.of(filterRequest.page() - 1, filterRequest.size(), Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Form129 getById(long id) {
        return form129Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    @Transactional
    public Form129 update(Form129Request request, Long id, User user) {
        Form129 existedEntity = form129Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        form129Mapper.update(existedEntity, request);

        Form129 saved = form129Repository.save(existedEntity);

        log.info("form129.update.success formId={} actorUserId={}", saved.getId(), user.getId());

        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long formId) {
        Form129 form129 = getById(formId);

        form129Repository.delete(form129);

        log.info("form129.delete.success formId={}", formId);
    }
}
