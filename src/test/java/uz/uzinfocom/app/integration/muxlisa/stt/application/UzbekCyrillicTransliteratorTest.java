package uz.uzinfocom.app.integration.muxlisa.stt.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class UzbekCyrillicTransliteratorTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            // o' / g' in every apostrophe flavour
            "o'zbek|ўзбек",
            "oʻzbek|ўзбек",
            "o‘zbek|ўзбек",
            "to’g’ri|тўғри",
            "tog'|тоғ",
            "O'qituvchi|Ўқитувчи",
            // sh / ch / ng
            "shahar|шаҳар",
            "chiqdi|чиқди",
            "Sh|Ш",
            "CHIROQ|ЧИРОҚ",
            "tong|тонг",
            "ming|минг",
            // e: word start / after vowel -> э, after consonant -> е
            "eshik|эшик",
            "ertalab|эрталаб",
            "telefon|телефон",
            "Ega|Эга",
            // ye / yo / yu / ya, plain y
            "yer|ер",
            "yo'l|йўл",
            "yosh|ёш",
            "Yuz|Юз",
            "yaxshi|яхши",
            "YOMON|ЁМОН",
            "yigit|йигит",
            "uy|уй",
            // ts: loanword -> ц, native boundary -> т+с
            "tsement|цемент",
            "sotsial|социал",
            "poliklinika|поликлиника",
            "otsiz|отсиз",
            "operatsiya|операция",
            // tutuq belgisi
            "ma'no|маъно",
            "sur'at|суръат",
            "a'zo|аъзо",
            "ta'sir|таъсир",
            // letters
            "hamshira|ҳамшира",
            "xona|хона",
            "qon|қон",
            "jarroh|жарроҳ",
            // punctuation and digits untouched
            "38,5 daraja!|38,5 даража!",
            "bir, ikki; uch: to'rt?|бир, икки; уч: тўрт?",
            "2025-yil 12-may|2025-йил 12-май",
            // already Cyrillic stays
            "мен dorixonaga bordim|мен дорихонага бордим",
    })
    void transliterates(String latin, String expected) {
        assertThat(UzbekCyrillicTransliterator.transliterate(latin)).isEqualTo(expected);
    }

    @Test
    void headlineExample() {
        assertThat(UzbekCyrillicTransliterator.transliterate("Telefon qilishadi. Chiqdi."))
                .isEqualTo("Телефон қилишади. Чиқди.");
    }

    @Test
    void quoteApostropheNotBetweenLettersIsKept() {
        assertThat(UzbekCyrillicTransliterator.transliterate("'salom' dedi"))
                .isEqualTo("'салом' деди");
    }

    @Test
    void emptyAndNullPassThrough() {
        assertThat(UzbekCyrillicTransliterator.transliterate("")).isEmpty();
        assertThat(UzbekCyrillicTransliterator.transliterate(null)).isNull();
    }
}
