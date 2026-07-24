package uz.uzinfocom.app.modules.act.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDailyCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.jpa.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregation queries for the home dashboard's act widget. {@link Act}, like
 * {@link uz.uzinfocom.app.modules.card.domain.model.Card}, has no
 * organization-id field of its own — scope is resolved by navigating
 * act -> card -> form058 and reusing {@link SenderReceiverScopePredicateFactory}
 * verbatim. {@code received} is always {@code true} for the same reason as
 * {@link uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardStatsRepository}.
 * Act is soft-deletable via {@code deleteInfo.deleted} like Form058/Form0581,
 * so the base class's default {@code notDeleted} applies unmodified.
 */
@Repository
public class ActStatsRepository extends AbstractCaseStatsRepository<Act> {

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public ActStatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Act.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    public List<ActStatusCountResponse> countByStatus(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<ActStatus>get("actStatus"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("card").get("form058"), cb, scope, true),
                ActStatusCountResponse::new
        );
    }

    /** Total act count in scope — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope) {
        return countAll((root, cb) ->
                scopePredicateFactory.applyDirectionScope(root.get("card").get("form058"), cb, scope, true));
    }

    /**
     * Monthly trend — for the home dashboard's act dynamics chart, same
     * shape/window as {@code Form058StatsRepository.countByMonth}.
     */
    public List<ActDailyCountResponse> countByMonth(ResolvedOrganizationScope scope, LocalDate from, LocalDate to) {
        return countByDateBucket(
                "month",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("card").get("form058"), cb, scope, true),
                from, to,
                ActDailyCountResponse::new
        );
    }
}
