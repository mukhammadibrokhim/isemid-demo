package uz.uzinfocom.isemid.models.entities.enums;

import lombok.Getter;

@Getter
public enum ActType {
    ACT153("Suv namunasi olish Dalolatnomasi (AKT)"),
    ACT154("OZIQ- OVQAT  MAHSULOTLARIDAN   NAMUNALAR   OLISH  BO‘YICHA DALOLATNOMA"),
    ACT155("Qishloq xo‘jaligi va oziq-ovqat maxsulotlari namunalarida pestitsidlar qoldiq miqdorlarini aniqlash bo‘yicha dalolatnoma."),
    ACT156("TASHQI MUHIT ANJOMLARIDAN   SURTMALAR OLISH   BUYICHA  DALOLATNOMA"),
    ACT223("TUPROQ  NAMUNALARINI  TEKSHIRISH Bo‘YICHA DALOLATNOMA"),
    ACT224("Sanitariya-epidemiologiya tekshiruvi o‘tkazish to‘g‘risidagi dalolatnoma");

    private final String description;

    ActType(String description) {
        this.description = description;
    }
}

