package client.formatting;

import java.util.Iterator;

/**
 * Parsib Markdown'i-laadset süntaksis tekstilõikudega, mil on ühene stiil.
 * Implementeeritud iteraatorina.
 */
public class FormattingParser implements Iterator<TextSection> {
    private static final String BOLD_DELIMITER = "**";
    private static final String ITALIC_DELIMITER = "*";
    private static final String MENTION_PREFIX = "@";

    private final CharSequence src;

    // Praegune stiili olek, seeda muudame jooksvalt.
    private final Style currentStyle = new Style();

    // Lugemispea, mida nihutame edasi, et sisendis edasi liikuda.
    private int cursor = 0;

    public FormattingParser(CharSequence src) {
        this.src = src;
    }

    // TODO: kas lubada täpitähti nimedes?
    private static boolean allowedInUsername(char ch) {
        return ch >= 'A' && ch <= 'Z' ||
                ch >= 'a' && ch <= 'z' ||
                ch >= '0' && ch <= '9' ||
                ch == '_';
    }

    @Override
    public boolean hasNext() {
        return cursor < src.length();
    }

    @Override
    public TextSection next() {
        // Kui on mention (@kasutaja süntaks), siis sellele muud stiilid ei
        // kehti, niisiis loeme selle kohe ära.
        if (currentlyStartingWith(MENTION_PREFIX)) {
            TextSection mention = takeMention();
            if (mention != null) {
                return mention;
            }
        }

        // Kui algab/lõpeb bold, tuleb praegust stiili muuta.
        checkForBold();
        // Samamoodi kaldkirjaga.
        checkForItalics();

        return takeSection();
    }


    /**
     * Loeb mention'i.
     *
     * @return stiiliga tekstilõik, mis sisaldab @ sümbolit ja kasutajanime (nt @kasutaja).
     */
    private TextSection takeMention() {
        assert currentlyStartingWith(MENTION_PREFIX); // Kindluse mõttes

        // Vaatame, kus kasutajanimi asub.
        int start = cursor;
        while (cursor < src.length()) {
            if (cursor > start && !allowedInUsername(src.charAt(cursor))) {
                break;
            }
            cursor++;
        }

        // Kasutajanimi on leitud, loeme selle sisse.
        CharSequence subSeq = src.subSequence(start, cursor);
        if (subSeq.isEmpty()) {
            // Tühi kasutajanimi, ilmselt oleme sisendi lõpus.
            return null;
        }

        // Muud stiilid mention'itele ei kehti, seega loome uue Style objekti.
        Style mentionStyle = new Style();
        mentionStyle.setMention(true);

        return new TextSection(subSeq, mentionStyle);
    }

    /**
     * Kontrollib, kas praegune sisend algab antud sõnega ja skip'ib selle.
     *
     * @param delimiter sõne, mis peaks alguses olema.
     * @return kas algas selle sõnega?
     */
    private boolean checkFor(CharSequence delimiter) {
        if (currentlyStartingWith(delimiter)) {
            skip(delimiter.length());
            return true;
        }
        return false;
    }

    /**
     * Kui oleme bold'i alguses/lõpus, uuendame praegust stiili.
     */
    private void checkForBold() {
        if (checkFor(BOLD_DELIMITER)) {
            currentStyle.setBold(!currentStyle.isBold());
        }
    }

    /**
     * Kui oleme kaldkirja'i alguses/lõpus, uuendame praegust stiili.
     */
    private void checkForItalics() {
        if (checkFor(ITALIC_DELIMITER)) {
            currentStyle.setItalic(!currentStyle.isItalic());
        }
    }

    /**
     * Jätab {@code n} tähemärki vahele
     */
    private void skip(int n) {
        cursor += n;
    }

    /**
     * Loeb teksilõigi senimaani, kuni on eriline sümbol, mis muudab stiili.
     *
     * @return loetud tekstilõik
     */
    private TextSection takeSection() {
        // Mõõdame, kui palju saame lugeda.
        int start = cursor;
        while (cursor < src.length() && !isSpecialDelimiter()) {
            cursor++;
        }

        // Loeme sisse.
        CharSequence subSeq = src.subSequence(start, cursor);

        // Tagastame koos praeguse stiiliga!
        return new TextSection(subSeq, currentStyle);
    }

    private boolean isSpecialDelimiter() {
        return currentlyStartingWith(BOLD_DELIMITER) ||
                currentlyStartingWith(ITALIC_DELIMITER) ||
                currentlyStartingWith(MENTION_PREFIX);
    }

    /**
     * Tagastab, kas praegusel positsioonil algab sisend antud prefiksiga.
     *
     * @param prefix prefiks
     * @return kas algab sellega?
     */
    private boolean currentlyStartingWith(CharSequence prefix) {
        // Pole piisavalt ruumi, ei saa sellega alata.
        if (src.length() - cursor < prefix.length()) {
            return false;
        }

        // Kontrollime ükshaaval tähemärke.
        for (int i = 0; i < prefix.length(); i++) {
            if (src.charAt(cursor + i) != prefix.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
