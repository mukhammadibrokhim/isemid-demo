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
        Long actorUserId,
        String actorUsername,
        String actorFullName,
        String reason,
        Map<String, Object> changes,
        Instant occurredAt
) {
}
//[Yaratish: frontend/integration/DMED, source belgilanadi] → status = SENT
//
//        │
//
//        ├─► accept (receiver tashkilot) → ACCEPTED
//        │        │
//        │        ▼ linkCards() (majburiy — approve shundan keyingina) → CARD_LINKED
//        │        │
//        │        └─► approve (sender tashkilot, finalIcd10 majburiy) → APPROVED (yakuniy)
//        │
//        └─► cancel (sender YOKI receiver, sabab bilan, faqat SENT bosqichida) →
//                 CANCELED (yakuniy, qulflangan — accept bo'lgandan keyin
//                 hech kim bekor qila olmaydi — faqat super_admin reopen
//                 orqali qayta SENT ga qaytaradi)
//
//
//
//        (SENT/ACCEPTED/CARD_LINKED bosqichlarida sender istalgan vaqt cancel qila oladi) 