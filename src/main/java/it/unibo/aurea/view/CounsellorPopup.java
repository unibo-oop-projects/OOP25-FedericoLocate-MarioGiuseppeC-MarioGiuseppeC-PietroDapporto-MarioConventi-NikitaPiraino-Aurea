package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Modal popup showing a counsellor giving advice when a parameter
 * crosses a critical threshold (low - under 10 or high - over 90).
 *
 * <p>Designed as a utility class because counsellor messages are stateless:
 * each invocation builds a brand new modal stage, shows it, then disposes it.
 */
public final class CounsellorPopup {

    private static final Logger LOGGER = Logger.getLogger(CounsellorPopup.class.getName());

    private static final int POPUP_WIDTH = 460;
    private static final int POPUP_HEIGHT = 280;
    private static final int CONTENT_SPACING = 14;
    private static final int CONTENT_PADDING = 24;
    private static final int PORTRAIT_SIZE = 180;

    private CounsellorPopup() {
        // Utility class
    }

    /**
     * Shows the counsellor popup with a custom message.
     * Blocks until the user dismisses the popup.
     *
     * @param message the advice text to display
     */
    public static void show(final String message) {
        AudioManager.playPopUp();
        final Stage popUp = new Stage();
        popUp.initStyle(StageStyle.UTILITY);
        popUp.initModality(Modality.APPLICATION_MODAL);
        popUp.setTitle("A word from your counsellor");
        popUp.setResizable(false);

        final ImageView portrait = loadCounsellorPortrait();

        final Label text = new Label(Objects.requireNonNullElse(message, ""));
        text.setWrapText(true);
        text.setTextAlignment(TextAlignment.LEFT);
        text.getStyleClass().add("counsellor-message");

        final HBox top = new HBox(CONTENT_SPACING);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(portrait, text);

        final Button dismiss = new Button("Understood!");
        dismiss.getStyleClass().add("counsellor-dismiss");
        dismiss.setOnAction(e -> popUp.close());

        final VBox content = new VBox(CONTENT_SPACING);
        content.setPadding(new Insets(CONTENT_PADDING));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("counsellor-content");
        content.getChildren().addAll(top, dismiss);

        final Scene scene = new Scene(content, POPUP_WIDTH, POPUP_HEIGHT);
        final var stylesheet = CounsellorPopup.class.getResource("/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        popUp.setScene(scene);
        popUp.showAndWait();
    }

    private static ImageView loadCounsellorPortrait() {
        try (InputStream is = CounsellorPopup.class.getResourceAsStream("/consigliere.png")) {
            if (Objects.nonNull(is)) {
                final ImageView img = new ImageView(new Image(is));
                img.setFitWidth(PORTRAIT_SIZE);
                img.setFitHeight(PORTRAIT_SIZE);
                img.setPreserveRatio(true);
                return img;
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Counsellor portrait could not be loaded", e);
        }
        return new ImageView();
    }
}
