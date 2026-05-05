package client.ui;

import client.formatting.FormattingParser;
import client.formatting.TextSection;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;

/**
 * Esindab üht sõnumit UI komponendina (võimaldab tekstivormingut).
 */
public class MessageComponent extends TextFlow {
    long time;
    long id;
    public MessageComponent(String author, String rawContent, long time, long id) {
        this.time = time;
        this.id = id;
        // Näitame sõnumi autorit.
        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("pirukas-bold");
        authorLabel.setTextFill(pickUserColor(author));

        // Autori ja sisu vahele koolon.
        Label separator = new Label(": ");
        getChildren().addAll(authorLabel, separator);

        // Parsime sõnumi vormindatud kujule ja lisame sektsiooni kaupa.
        FormattingParser formattingParser = new FormattingParser(rawContent);
        while (formattingParser.hasNext()) {
            TextSection section = formattingParser.next();
            getChildren().add(section.asLabel());
        }
    }

    /**
     * Valib värvi lähtudes kasutajanimest.
     *
     * @param username kasutajanimi
     * @return värv
     */
    private static Color pickUserColor(String username) {
        // Ma lasin AI-l genereerida mingi suvalise paletti.
        // TODO: pole mõtet neid sõnedena hoida, tuleks kohe Color'iteks teha.
        String[] colors = {"#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231", "#911eb4", "#42d4f4", "#f032e6", "#bfef45", "#fabed4", "#469990", "#dcbeff", "#9a6324", "#fffac8", "#800000", "#aaffc3"};

        // NB: absoluutväärtus on vajalik, kuna hashCode on int tüüpi ja võib
        // seega olla negatiivne (Java on loll keel).
        return Color.valueOf(colors[Math.abs(username.hashCode()) % colors.length]);
    }
}