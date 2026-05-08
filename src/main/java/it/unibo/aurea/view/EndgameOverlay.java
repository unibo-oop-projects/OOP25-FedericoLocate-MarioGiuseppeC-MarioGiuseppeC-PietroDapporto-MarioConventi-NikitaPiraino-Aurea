package it.unibo.aurea.view;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Full-screen overlay shown when the game ends (victory, defeat, or game-over).
 *
 * <p>Displays a title, a narrative subtitle, and a recap of the final values
 * of the four parameters. The overlay fades in over {@code FADE_MILLIS}
 * milliseconds and is mouse-transparent so it does not interact with input.
 */
public final class EndgameOverlay extends VBox {

    private static final int CONTAINER_SPACING = 18;
    private static final int RECAP_SPACING_H = 30;
    private static final int RECAP_SPACING_V = 8;
    private static final int BUTTON_ROW_SPACING = 16;
    private static final int BUTTON_ROW_TOP_PADDING = 24;
    private static final double FADE_MILLIS = 900;

    private static final String BG_VICTORY = "rgba(0, 0, 0, 0.82)";
    private static final String BG_DEFEAT = "rgba(20, 0, 0, 0.88)";

    private final Label titleLabel;
    private final Label subtitleLabel;
    private final GridPane recapGrid;
    private final HBox buttonRow;
    private final Runnable onRestart;

    /**
     * Builds the overlay (initially invisible).
     *
     * @param onRestart callback invoked when the player chooses to play again
     */
    public EndgameOverlay(final Runnable onRestart) {
        this.onRestart = onRestart;

        setAlignment(Pos.CENTER);
        setSpacing(CONTAINER_SPACING);
        setMouseTransparent(false);
        setOpacity(0);
        setVisible(false);
        getStyleClass().add("endgame-overlay");

        this.titleLabel = new Label();
        this.titleLabel.getStyleClass().add("endgame-title");

        this.subtitleLabel = new Label();
        this.subtitleLabel.getStyleClass().add("endgame-subtitle");
        this.subtitleLabel.setWrapText(true);

        this.recapGrid = new GridPane();
        this.recapGrid.setAlignment(Pos.CENTER);
        this.recapGrid.setHgap(RECAP_SPACING_H);
        this.recapGrid.setVgap(RECAP_SPACING_V);
        this.recapGrid.getStyleClass().add("endgame-recap");

        this.buttonRow = buildButtonRow();

        getChildren().addAll(titleLabel, subtitleLabel, recapGrid, buttonRow);
    }

    private HBox buildButtonRow() {
        final Button quitBtn = new Button("Leave the Realm");
        quitBtn.getStyleClass().add("endgame-button-quit");
        quitBtn.setOnAction(e -> javafx.application.Platform.exit());

        final Button restartBtn = new Button("Reign Again");
        restartBtn.getStyleClass().add("endgame-button-restart");
        restartBtn.setOnAction(e -> {
            setVisible(false);
            setOpacity(0);
            onRestart.run();
        });

        final HBox row = new HBox(BUTTON_ROW_SPACING);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(BUTTON_ROW_TOP_PADDING, 0, 0, 0));
        row.getChildren().addAll(quitBtn, restartBtn);
        return row;
    }

    /**
     * Reveals the overlay with the given content and a fade-in animation.
     *
     * @param title the headline text
     * @param subtitle the narrative explanation
     * @param finalLevels snapshot of the four parameters at game end
     * @param victory    true for victory (golden tint), false for defeat (red tint)
     */
    public void reveal(final String title, final String subtitle, 
        final Map<ParameterType, Integer> finalLevels,
        final boolean victory) {
            titleLabel.setText(title);
            subtitleLabel.setText(subtitle);
            populateRecap(finalLevels);

            final String bg = victory ? BG_VICTORY : BG_DEFEAT;
            setStyle("-fx-background-color: " + bg + "; -fx-padding: 80 40 80 40;");
            setVisible(true);
            setMouseTransparent(false);

            final FadeTransition fade = new FadeTransition(Duration.millis(FADE_MILLIS), this);
            fade.setFromValue(0);
            fade.setToValue(1.0);
            fade.play();
    }

    private void populateRecap(final Map<ParameterType, Integer> finalLevels) {
        recapGrid.getChildren().clear();
        if (finalLevels == null || finalLevels.isEmpty()) {
            return;
        }

        int row = 0;
        for (final ParameterType type : ParameterType.values()) {
            final Integer value = finalLevels.get(type);
            if (value == null) {
                continue;
            }
            final Label name = new Label(type.name());
            name.getStyleClass().add("endgame-recap-name");

            final Label number = new Label(String.valueOf(value));
            number.getStyleClass().add("endgame-recap-value");

            recapGrid.add(name, 0, row);
            recapGrid.add(number, 1, row);
            row++;
        }
    }
}
