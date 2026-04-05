package client.formatting;

import javafx.scene.control.Label;

/**
 * Esindab tekstilõiku, millel on ühene stiil.
 */
public class TextSection {
    private final CharSequence content;
    private final Style style;

    public TextSection(CharSequence content, Style style) {
        this.content = content;
        this.style = style;
    }

    /**
     * Tagastab stiiliga sisu kujul, mis on sobilik terminali trükkimiseks.
     *
     * @return ANSI koodidega sisu
     */
    public String asANSIFormatted() {
        StringBuilder sb = new StringBuilder();

        if (style.isBold()) sb.append("\u001b[1m");
        if (style.isItalic()) sb.append("\u001b[3m");
        if (style.isMention()) sb.append("\u001b[37;46m");

        sb.append(content);

        if (style.isMention()) sb.append("\u001b[39;49m");
        if (style.isItalic()) sb.append("\u001b[23m");
        if (style.isBold()) sb.append("\u001b[22m");

        return sb.toString();
    }

    /**
     * Tagastab stiiliga sisu.
     * @return stiiliga sisu, seekord JavaFX komponendina.
     */
    public Label asLabel() {
        Label label = new Label(content.toString());

        // Need stiilid on defineeritud stylesheet'is, vt ressursse.
        if (style.isBold()) label.getStyleClass().add("pirukas-bold");
        if (style.isItalic()) label.getStyleClass().add("pirukas-italic");
        if (style.isMention()) label.getStyleClass().add("pirukas-mention");

        return label;
    }

    @Override
    public String toString() {
        return "TextSection{" +
                "content=" + content +
                ", style=" + style +
                '}';
    }
}
