package uz.uzinfocom.isemid.features.act.enums;

public enum ActStatus {
    NEW,            // yangi yaratilgan
    IN_PROGRESS,    // jarayonda (masalan, tahrirlanmoqda)
    SENT,           // LISga yuborilgan
    PENDING,           // LISga yuborilmoqda
    RECEIVED,       // LISdan javob keldi
    COMPLETED,      // to‘liq yakunlangan
    NOT_VIEWED,     // hali ko‘rilmagan
    ACT_ATTACHED,   // kartaga biriktirilgan
    FAILED          // yuborish / qabul qilishda xato
}
