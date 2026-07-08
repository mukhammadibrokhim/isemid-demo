package uz.uzinfocom.app.platform.reference.application.mkb10.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;

/**
 * Resolves a parent {@link Mkb10} reference by external id for MapStruct mappings,
 * without eagerly loading the parent row — {@link EntityManager#getReference} returns
 * a lazy proxy that only needs the id to be attached as the child's foreign key.
 */
@Component
@RequiredArgsConstructor
public class Mkb10ParentResolver {

    private final EntityManager entityManager;

    @Named("resolveMkb10Parent")
    public Mkb10 resolveParent(Long parentId) {
        return parentId == null ? null : entityManager.getReference(Mkb10.class, parentId);
    }
}
