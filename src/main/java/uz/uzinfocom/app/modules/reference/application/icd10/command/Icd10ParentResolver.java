package uz.uzinfocom.app.modules.reference.application.icd10.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.reference.domain.Icd10;

/**
 * Resolves a parent {@link Icd10} reference by external id for MapStruct mappings,
 * without eagerly loading the parent row — {@link EntityManager#getReference} returns
 * a lazy proxy that only needs the id to be attached as the child's foreign key.
 */
@Component
@RequiredArgsConstructor
public class Icd10ParentResolver {

    private final EntityManager entityManager;

    @Named("resolveIcd10Parent")
    public Icd10 resolveParent(Long parentId) {
        return parentId == null ? null : entityManager.getReference(Icd10.class, parentId);
    }
}
