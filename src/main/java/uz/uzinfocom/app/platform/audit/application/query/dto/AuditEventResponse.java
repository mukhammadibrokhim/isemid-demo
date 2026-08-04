package uz.uzinfocom.app.platform.audit.application.query.dto;

import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditEventType;

import java.time.Instant;
import java.util.Map;

public record AuditEventResponse(
        Long id,
        AuditEventType eventType,
        AuditEntityType entityType,
        Long entityId,
        String oldStatus,
        String newStatus,
        Long oldOrgId,
        Long newOrgId,
        Long actorUserId,
        String reason,
        Map<String, Object> changes,
        Instant occurredAt
) {
}
//[Yaratish: frontend/integration/DMED, source belgilanadi] → status = SENT
//
//        │
//
//        ▼ (ixtiyoriy: card biriktirilsa)
//
//   linkCards() → status = CARD_LINKED
//
//        │
//
//        ├─► approve (receiver tashkilot, finalIcd10 majburiy) → APPROVED (yakuniy)
//
//        ├─► not-approve (sabab bilan) → NOT_APPROVED (qayta approve bo'lishi mumkin — pending qayta ochiladi)
//
//        └─► cancel (sender, APPROVED bo'lmagunicha) → CANCELED (yakuniy)
//
//
//
//bu form058 ni asosiy flowi lekin bunday bolmaydi. 