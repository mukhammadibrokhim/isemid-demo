package uz.uzinfocom.isemid.models.entities.card;

import lombok.Getter;

@Getter
public enum CardStatus {
    NEW,              // Yangi yaratildi (Hali hech kim ishlamagan)
    IN_PROGRESS,      // Xodimlar ishlayapti
    COMPLETED,        // Xodim ishni tugatdi (save complete)
    ACCEPTED_BY_USER, // Xodim ma'lumotlarni to‘g‘ri deb qabul qildi
    REJECTED_BY_USER, // Xodim noto‘g‘ri deb rad etdi
    APPROVED,         // Zavedenie tasdiqladi
    REJECTED ,
    ACT_ATTACHED// Zavedenie rad etdi
}
