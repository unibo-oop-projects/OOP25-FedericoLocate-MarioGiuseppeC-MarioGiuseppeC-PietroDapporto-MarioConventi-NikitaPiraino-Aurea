package it.unibo.aurea.view;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Full-screen overlay shown when the game ends.
 *
 * <p>Fades in a title and a subtitle on top of a darkened, parchment-like
 * background. The overlay sits above the rest of the scene and is invisible
 * by default until {@link #reveal(String, String)} is invoked.
 */
public final class EndgameOverlay extends VBox {

    private static final int FADE_IN_MILLIS = 900;
    private static final int PADDING = 40;
    private static final int SPACING = 20;
    private static final int TITLE_FONT_SIZE = 36;
    private static final int SUBTITLE_FONT_SIZE = 20;
    private static final double BG_RED = 10.0 / 255.0;
    private static final double BG_GREEN = 6.0 / 255.0;
    private static final double BG_BLUE = 3.0 / 255.0;
    private static final double BG_ALPHA = 0.92;

    private static final String FONT_STORY = "'IM Fell English', 'Georgia', serif";
    private static final String COLOR_TITLE = "#c4a06a";
    private static final String COLOR_SUBTITLE = "#e8d5b0";

    private final Label title;
    private final Label subtitle;

    /**
     * Builds an empty, hidden overlay ready to be revealed.
     */
    public EndgameOverlay() {
        setAlignment(Pos.CENTER);
        setSpacing(SPACING);
        setPadding(new Insets(PADDING));
        setBackground(new Background(new BackgroundFill(
            new Color(BG_RED, BG_GREEN, BG_BLUE, BG_ALPHA),
            CornerRadii.EMPTY, Insets.EMPTY)));
        setMouseTransparent(true);

        this.title = buildLabel(TITLE_FONT_SIZE, COLOR_TITLE);
        this.subtitle = buildLabel(SUBTITLE_FONT_SIZE, COLOR_SUBTITLE);

        getChildren().addAll(title, subtitle);
        setOpacity(0);
        setVisible(false);
    }

    private Label buildLabel(final int fontSize, final String color) {
        final Label label = new Label();
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setStyle(
            "-fx-font-family: " + FONT_STORY + ";"
            + "-fx-font-size: " + fontSize + "px;"
            + "-fx-text-fill: " + color + ";"
            + "-fx-effect: dropshadow(gaussian, black, 6, 0.5, 0, 0);"
        );
        return label;
    }

    /**
     * Reveals the overlay with a fade-in animation.
     *
     * @param titleText the main heading
     * @param subtitleText the supporting text shown beneath the title
     */
    public void reveal(final String titleText, final String subtitleText) {
        this.title.setText(titleText);
        this.subtitle.setText("« " + subtitleText + " »");
        setVisible(true);
        final FadeTransition fade = new FadeTransition(Duration.millis(FADE_IN_MILLIS), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
