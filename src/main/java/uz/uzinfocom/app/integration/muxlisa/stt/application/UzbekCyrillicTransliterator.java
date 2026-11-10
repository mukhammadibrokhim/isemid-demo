package uz.uzinfocom.app.integration.muxlisa.stt.application;

import java.util.Locale;

/**
 * Uzbek Latin -> Uzbek Cyrillic, the single place the speech-to-text answer is converted
 * when the caller asked for {@code uz-Cyrl}. Muxlisa answers in Latin only (as far as we
 * know), so this runs on its text.
 *
 * <p>Rules (standard Uzbek orthography):
 * <ul>
 *   <li>{@code o'} -> ў, {@code g'} -> ғ (any apostrophe variant: {@code ' ʼ ʻ ‘ ’ ` ´});</li>
 *   <li>{@code sh} -> ш, {@code ch} -> ч, {@code ng} -> нг (plain letters, no special glyph);</li>
 *   <li>{@code yo/yu/ya/ye} -> ё/ю/я/е, otherwise {@code y} -> й;</li>
 *   <li>{@code e} -> э at the start of a word or after a vowel, otherwise е;</li>
 *   <li>{@code ts} -> ц only where it is a loanword affix: at the start of a word or before
 *       {@code iya/ion/ial/iy}; elsewhere it is two letters (о'tsiz is "отсиз", not "оциз");</li>
 *   <li>an apostrophe between two letters (tutuq belgisi) -> ъ; a lone/quote apostrophe is kept;</li>
 *   <li>h -> ҳ, x -> х, q -> қ, j -> ж; digits, punctuation and text already in Cyrillic are untouched;</li>
 *   <li>case is preserved ({@code Sh}/{@code SH} -> Ш).</li>
 * </ul>
 */
public final class UzbekCyrillicTransliterator {

    private static final String APOSTROPHES = "'ʼʻ‘’`´";
    private static final String VOWELS = "aeiou";

    private UzbekCyrillicTransliterator() {
    }

    public static String transliterate(String latin) {
        if (latin == null || latin.isEmpty()) {
            return latin;
        }
        StringBuilder out = new StringBuilder(latin.length());
        int length = latin.length();
        int i = 0;
        while (i < length) {
            char c = latin.charAt(i);
            char lower = Character.toLowerCase(c);
            boolean upper = Character.isUpperCase(c);
            char next = i + 1 < length ? Character.toLowerCase(latin.charAt(i + 1)) : '\0';
            char previous = i > 0 ? Character.toLowerCase(latin.charAt(i - 1)) : '\0';

            if (isApostrophe(c)) {
                // o'/g' are handled with their letter below; this is a tutuq between two letters.
                boolean between = isLatinLetter(previous) && i + 1 < length && isLatinLetter(next);
                out.append(between ? "ъ" : String.valueOf(c));
                i++;
                continue;
            }
            if (!isLatinLetter(lower)) {
                out.append(c);
                i++;
                continue;
            }

            if ((lower == 'o' || lower == 'g') && i + 1 < length && isApostrophe(latin.charAt(i + 1))) {
                out.append(cased(lower == 'o' ? "ў" : "ғ", upper));
                i += 2;
                continue;
            }
            if (next != '\0' && (lower == 's' || lower == 'c') && next == 'h') {
                out.append(cased(lower == 's' ? "ш" : "ч", upper));
                i += 2;
                continue;
            }
            if (lower == 'y' && next != '\0') {
                String mapped = switch (next) {
                    case 'o' -> "ё";
                    case 'u' -> "ю";
                    case 'a' -> "я";
                    case 'e' -> "е";
                    default -> null;
                };
                // "yo'l" is й+ў, not ё+apostrophe: the o belongs to the o' digraph.
                boolean oDigraph = next == 'o' && i + 2 < length && isApostrophe(latin.charAt(i + 2));
                if (mapped != null && !oDigraph) {
                    out.append(cased(mapped, upper));
                    i += 2;
                    continue;
                }
            }
            if (lower == 't' && next == 's' && isTsLoanword(latin, i, previous)) {
                out.append(cased("ц", upper));
                i += 2;
                continue;
            }
            if (lower == 'e') {
                boolean hard = !isLatinLetter(previous) || VOWELS.indexOf(previous) >= 0;
                out.append(cased(hard ? "э" : "е", upper));
                i++;
                continue;
            }

            out.append(cased(single(lower), upper));
            i++;
        }
        return out.toString();
    }

    private static boolean isTsLoanword(String text, int index, char previous) {
        if (!isLatinLetter(previous)) {
            return true;
        }
        String rest = text.substring(index + 2).toLowerCase(Locale.ROOT);
        return rest.startsWith("iya") || rest.startsWith("ion") || rest.startsWith("ial") || rest.startsWith("iy");
    }

    private static String single(char lower) {
        return switch (lower) {
            case 'a' -> "а";
            case 'b' -> "б";
            case 'c' -> "ц";
            case 'd' -> "д";
            case 'f' -> "ф";
            case 'g' -> "г";
            case 'h' -> "ҳ";
            case 'i' -> "и";
            case 'j' -> "ж";
            case 'k' -> "к";
            case 'l' -> "л";
            case 'm' -> "м";
            case 'n' -> "н";
            case 'o' -> "о";
            case 'p' -> "п";
            case 'q' -> "қ";
            case 'r' -> "р";
            case 's' -> "с";
            case 't' -> "т";
            case 'u' -> "у";
            case 'v', 'w' -> "в";
            case 'x' -> "х";
            case 'y' -> "й";
            case 'z' -> "з";
            default -> String.valueOf(lower);
        };
    }

    private static String cased(String cyrillic, boolean upper) {
        return upper ? cyrillic.toUpperCase(Locale.ROOT) : cyrillic;
    }

    private static boolean isApostrophe(char c) {
        return APOSTROPHES.indexOf(c) >= 0;
    }

    private static boolean isLatinLetter(char lower) {
        return lower >= 'a' && lower <= 'z';
    }
}
