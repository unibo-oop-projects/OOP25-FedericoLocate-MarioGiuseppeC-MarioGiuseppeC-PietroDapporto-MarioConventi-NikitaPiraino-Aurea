package it.unibo.aurea.view;

import java.util.Map;
import java.util.Objects;

import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.Report;
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
 * implementation usable for intermatiate and final stages of the game.
 */
public final class ReportImpl extends VBox implements Report {

    private static final int CONTAINER_SPACING = 18;
    private static final int RECAP_SPACING_H = 30;
    private static final int RECAP_SPACING_V = 8;
    private static final int BUTTON_ROW_TOP_PADDING = 24;
    private static final double FADE_MILLIS = 900;

    private static final String BG_SEMESTER = "rgba(0, 0, 0, 0.82)";

    private final Label titleLabel;
    private final Label subtitleLabel;
    private final GridPane recapGrid;

    /**
     * Builds the report overlay (not yet visible).
     */
    public ReportImpl() {
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
        this.recapGrid.setMaxWidth(USE_PREF_SIZE);

        final Button continueBtn = new Button("Continue");
        continueBtn.getStyleClass().add("counsellor-dismiss");
        continueBtn.setPadding(new Insets(BUTTON_ROW_TOP_PADDING, 0, 0, 0));
        continueBtn.setOnAction(e -> close());

        getChildren().addAll(titleLabel, subtitleLabel, recapGrid, continueBtn);
    }

    @Override
    public void show(final String semesterLabel, final Map<ParameterType, Integer> levels) {
        Objects.requireNonNull(semesterLabel, "semesterLabel cannot be null");
        Objects.requireNonNull(levels, "levels cannot be null");
        titleLabel.setText("End of " + semesterLabel);
        subtitleLabel.setText("Here is the state of the Realm at the close of this semester.");
        populateRecap(levels);

        setStyle("-fx-background-color: " + BG_SEMESTER + "; -fx-padding: 80 40 80 40;");
        setVisible(true);
        setMouseTransparent(false);

        final FadeTransition fade = new FadeTransition(Duration.millis(FADE_MILLIS), this);
        fade.setFromValue(0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void populateRecap(final Map<ParameterType, Integer> levels) {
        recapGrid.getChildren().clear(); //this is made to remove the old values without creating a new gridPane or Report.
        if (levels == null || levels.isEmpty()) {
            return;
        }
        int row = 0;
        for (final ParameterType type : ParameterType.values()) {
            final Integer value = levels.get(type);
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

    @Override
    public void setTitle(final String title) {
        Objects.requireNonNull(title, "title cannot be null");
        titleLabel.setText(title);
    }

    @Override
    public void setSubtitle(final String subtitle) {
        Objects.requireNonNull(subtitle, "subtitle cannot be null");
        subtitleLabel.setText(subtitle);
    }

    @Override
    public void setLevels(final Map<ParameterType, Integer> levels) {
        Objects.requireNonNull(levels, "levels cannot be null");
        populateRecap(levels);
    }

    @Override
    public void setButtonAction(final HBox buttonRow) {
        Objects.requireNonNull(buttonRow, "buttonRow cannot be null");
        getChildren().remove(getChildren().size() - 1);
        getChildren().add(buttonRow);
    }

    @Override
    public void reveal(final String background) {
        Objects.requireNonNull(background, "background cannot be null");
        setStyle("-fx-background-color: " + background + "; -fx-padding: 80 40 80 40;");
        setVisible(true);
        setMouseTransparent(false);

        final FadeTransition fade = new FadeTransition(Duration.millis(FADE_MILLIS), this);
        fade.setFromValue(0);
        fade.setToValue(1.0);
        fade.play();
    }

    @Override
    public void close() {
        setVisible(false);
        setOpacity(0);
    }

    @Override
    public void applyStyle(final boolean victory) {
        titleLabel.getStyleClass().removeAll("endgame-title-victory", "endgame-title-defeat");
        titleLabel.getStyleClass().add(victory ? "endgame-title-victory" : "endgame-title-defeat");
    }
}
