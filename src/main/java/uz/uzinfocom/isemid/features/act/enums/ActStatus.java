package uz.uzinfocom.isemid.features.act.enums;

public enum ActStatus {
    NEW,            // yangi yaratilgan
    IN_PROGRESS,    // jarayonda (masalan, tahrirlanmoqda)
    SENT,           // LISga yuborilgan
    PENDING,           // LISga yuborilmoqda
    ACCEPTED_BY_TESTER,       // LISdan javob keldi
    REJECTED_BY_TESTER,       // LISdan rad javob keldi
    CONCLUSION_PENDING,      // LIS tomondan kelgan protokol uchun akt yuboruvchi xulosasi kutilmoqda
    COMPLETED,      // to‘liq yakunlangan
    NOT_VIEWED,     // hali ko‘rilmagan
    ACT_ATTACHED,   // kartaga biriktirilgan
    FAILED          // yuborish / qabul qilishda xato
}
