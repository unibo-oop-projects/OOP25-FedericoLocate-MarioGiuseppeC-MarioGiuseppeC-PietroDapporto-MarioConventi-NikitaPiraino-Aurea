package it.unibo.aurea.view;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.Report;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * Full-screen overlay shown when the game ends (victory, defeat, or game-over).
 *
 * <p>Displays a title, a narrative subtitle, and a recap of the final values
 * of the four parameters. The overlay fades in over {@code FADE_MILLIS}
 * milliseconds and is mouse-transparent so it does not interact with input.
 */
public final class EndgameOverlay {

    private static final int BUTTON_ROW_SPACING = 16;
    private static final int BUTTON_ROW_TOP_PADDING = 24;

    private static final String BG_VICTORY = "rgba(0, 0, 0, 0.82)";
    private static final String BG_DEFEAT = "rgba(20, 0, 0, 0.88)";

    private final Report report;
    private final Runnable onRestart;

    /**
     * Builds the overlay.
     *
     * @param report    the shared report overlay used to display content
     * @param onRestart callback invoked when the player chooses to play again
     */
    public EndgameOverlay(final Report report, final Runnable onRestart) {
        this.report = report;
        this.onRestart = onRestart;
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
        report.setTitle(title);
        report.setSubtitle(subtitle);
        report.setLevels(finalLevels);
        report.setButtonAction(buildButtonRow());
        report.reveal(victory ? BG_VICTORY : BG_DEFEAT);
    }

    private HBox buildButtonRow() {
        final Button quitBtn = new Button("Leave the Realm");
        quitBtn.getStyleClass().add("endgame-button-quit");
        quitBtn.setOnAction(e -> javafx.application.Platform.exit());

        final Button restartBtn = new Button("Reign Again");
        restartBtn.getStyleClass().add("endgame-button-restart");
        restartBtn.setOnAction(e -> {
            report.close();
            onRestart.run();
        });

        final HBox row = new HBox(BUTTON_ROW_SPACING);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(BUTTON_ROW_TOP_PADDING, 0, 0, 0));
        row.getChildren().addAll(quitBtn, restartBtn);
        return row;
    }
}
