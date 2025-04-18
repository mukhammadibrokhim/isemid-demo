package uz.uzinfocom.isemid.features.card.enums;

public enum CardStatus {
    NEW,              // Yangi yaratildi (Hali hech kim ishlamagan)
    IN_PROGRESS,      // Xodimlar karta bilan ishlayapti
    COMPLETED,        // Xodim karta ishni tugatdi
    ACCEPTED_BY_USER, // Xodim ma'lumotlarni to‘g‘ri deb qabul Kartani qildi
    REJECTED_BY_USER, // Xodim noto‘g‘ri deb kartani rad etdi
    APPROVED,         // Bo'lim mudiri tasdiqladi
    REJECTED
}
