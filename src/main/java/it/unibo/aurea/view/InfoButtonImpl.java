package it.unibo.aurea.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Builds the info button that opens the rules popup.
 */
public class InfoButtonImpl {

    /**
     * Builds and returns the info button.
     *
     * @param colorNameGold    hex colour for the info icon
     * @param infoIconScale    scale factor for the info icon
     * @param paddingNormal    standard padding used inside the popup
     * @param rulesPopupWidth  width of the rules popup window
     * @param rulesPopupHeight height of the rules popup window
     * @return the configured {@link Button}
     */
    public Button build(
            final String colorNameGold,
            final double infoIconScale,
            final int paddingNormal,
            final int rulesPopupWidth,
            final int rulesPopupHeight) {
        final SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"
            + "m1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setFill(Color.web(colorNameGold));
        infoIcon.setScaleX(infoIconScale);
        infoIcon.setScaleY(infoIconScale);

        final Button btn = new Button();
        btn.setGraphic(infoIcon);
        btn.getStyleClass().add("info-button");
        btn.setOnAction(e -> showRules(paddingNormal, rulesPopupWidth, rulesPopupHeight));
        return btn;
    }

    private void showRules(final int paddingNormal, final int rulesPopupWidth, final int rulesPopupHeight) {
        final Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Tome of Decrees");
        popup.setResizable(false);

        final Label title = new Label("The Royal Decrees");
        title.getStyleClass().add("rules-title");

        final Label body = new Label(rulesText());
        body.setWrapText(true);
        body.getStyleClass().add("rules-body");

        final ScrollPane scroll = new ScrollPane(body);
        scroll.getStyleClass().add("rules-scroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(rulesPopupHeight - 100);

        final Button closeBtn = new Button("Close the Tome");
        closeBtn.getStyleClass().add("counsellor-dismiss");
        closeBtn.setOnAction(e -> popup.close());

        final VBox content = new VBox(12);
        content.setPadding(new Insets(paddingNormal));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("counsellor-content");
        content.getChildren().addAll(title, scroll, closeBtn);

        final Scene scene = new Scene(content, rulesPopupWidth, rulesPopupHeight);
        final var ss = getClass().getResource("/styles.css");
        if (ss != null) {
            scene.getStylesheets().add(ss.toExternalForm());
        }
        popup.setScene(scene);
        popup.showAndWait();
    }

    private static String rulesText() {
        return """
            Welcome, Magnificent Rector.

            Your task is to lead the University through three years of governance,
            balancing the Four Pillars of the Realm:

            FINANCES — the gold that sustains every ambition.
            STUDENTS — those who fill the halls and chant your name.
            PROFESSORS — the masters whose wisdom builds your legacy.
            REPUTATION — the voice of the public, swift to praise and to scorn.

            EACH SEMESTER you will face decisions, presented as cards bearing
            the words of those who depend on you. Swipe RIGHT to accept,
            LEFT to refuse. Each choice will tilt the Pillars.

            ENDGAME
            Your reign ends in glory if you survive the three full years.
            It ends in tragedy if any of the Four Pillars falls to ruin (0)
            or overflows in hubris (100).

            TIPS
            Watch the white dot above each pillar — it warns you which ones
            will be touched by your current decision. Hover with your eye
            to feel the weight of the choice before you make it.

            Your reign begins now.
            """;
    }
}
