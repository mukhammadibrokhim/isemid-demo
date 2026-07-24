package uz.uzinfocom.isemid.commons.utils;

public class TextNormalizationUtils {

    // o', g' kabi so'zlarda ishlatiladigan tutuq belgisining turli xil unicode variantlari
    public static final String APOSTROPHE_CHARS = "'ʻʼ‘’`´";

    public static String stripApostrophes(String value) {
        if (value == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (APOSTROPHE_CHARS.indexOf(c) < 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
