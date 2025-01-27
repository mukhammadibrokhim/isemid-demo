package uz.uzinfocom.isemid.models.entities.enums;

import lombok.Getter;

@Getter
public enum CardType {
    CARD161("Yuqumli kasalliklar"),
    CARD174("Zoonoz kasalliklar"),
    CARD175("Zoonoz kasalliklari"),
    CARD205("Hayvon tomonidan tishlangan va muloqotda yuqgan kasalliklar"),
    CARD_TUBE("Tuberkuloz kasalliklar");

    private final String description;

    CardType(String description) {
        this.description = description;
    }
}
