package it.unibo.aurea.view;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.Report;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * implementation usable for intermatiate and final stages of the game.
 */
public final class ReportImpl implements Report {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 360;
    private static final int SPACING = 18;
    private static final int RECAP_HGAP = 60;
    private static final int RECAP_VGAP = 8;
    private static final int PADDING = 24;
    private static final double FADE_MILLIS = 900;

    private final Stage stage;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final GridPane recapGrid;

    /**
     * Builds the report popup (not yet visible).
     */
    public ReportImpl() {
        this.stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        this.titleLabel = new Label();
        this.titleLabel.getStyleClass().add("endgame-title");
        this.titleLabel.setWrapText(true);

        this.subtitleLabel = new Label();
        this.subtitleLabel.getStyleClass().add("endgame-subtitle");
        this.subtitleLabel.setWrapText(true);

        this.recapGrid = new GridPane();
        this.recapGrid.setAlignment(Pos.CENTER);
        this.recapGrid.setHgap(RECAP_HGAP);
        this.recapGrid.setVgap(RECAP_VGAP);
        this.recapGrid.getStyleClass().add("endgame-recap");

        final Button continueBtn = new Button("Continue");
        continueBtn.getStyleClass().add("counsellor-dismiss");
        continueBtn.setOnAction(e -> close());

        final VBox content = new VBox(SPACING);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(PADDING));
        content.getStyleClass().add("counsellor-content");
        content.getChildren().addAll(titleLabel, subtitleLabel, recapGrid, continueBtn);

        final Scene scene = new Scene(content, WIDTH, HEIGHT);
        final var stylesheet = Report.class.getResource("/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setScene(scene);
    }

    @Override
    public void show(final String semesterLabel, final Map<ParameterType, Integer> levels) {
        stage.setTitle("Semester Report");
        titleLabel.setText("End of " + semesterLabel);
        subtitleLabel.setText("Here is the state of the Realm at the close of this session.");
        populateRecap(levels);

        final var root = stage.getScene().getRoot();
        root.setOpacity(0);
        stage.show();
        final FadeTransition fade = new FadeTransition(Duration.millis(FADE_MILLIS), root);
        fade.setFromValue(0);
        fade.setToValue(1.0);
        fade.play();
        stage.showAndWait();
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
    public void close() {
        stage.close();
    }
}
