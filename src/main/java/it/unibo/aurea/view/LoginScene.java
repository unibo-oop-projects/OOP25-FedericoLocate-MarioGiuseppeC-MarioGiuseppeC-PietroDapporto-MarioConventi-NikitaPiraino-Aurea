package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.controller.api.PlayerInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The first scene shown to the player. Asks for rector name and faculty.
 *
 * <p>Once the form is submitted, the {@code onLoginComplete} callback is invoked
 * with the collected {@link PlayerInfo}. The login stage is then closed and the
 * caller is responsible for opening the game stage.
 */

public final class LoginScene {

    private static final Logger LOGGER = Logger.getLogger(LoginScene.class.getName());

    private static final int SCENE_WIDTH = 600;
    private static final int SCENE_HEIGHT = 700;
    private static final int FORM_SPACING = 18;
    private static final int FORM_PADDING = 40;
    private static final int FIELD_WIDTH = 320;
    private static final int LOGO_SIZE = 220;

    private final Stage stage;
    private final Consumer<PlayerInfo> onLoginComplete;

    /**
     * Builds the login scene and shows it immediately.
     *
     * @param onLoginComplete callback invoked with the collected player info
     */
    public LoginScene(final Consumer<PlayerInfo> onLoginComplete) {
        this.stage = new Stage();
        this.onLoginComplete = Objects.requireNonNull(onLoginComplete);
        buildAndShow();
    }

    private void buildAndShow() {
        stage.setTitle("Aurea Mediocritas");
        stage.setResizable(false);

        final ImageView logo = loadLogo();

        final TextField rectorField = new TextField();
        rectorField.setPromptText("Your name, magnificent rector");
        rectorField.setMaxWidth(FIELD_WIDTH);
        rectorField.getStyleClass().add("login-field");

        final TextField facultyField = new TextField();
        facultyField.setPromptText("Your faculty, magnificent rector");
        facultyField.setMaxWidth(FIELD_WIDTH);
        facultyField.getStyleClass().add("login-field");

        final Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);

        final Button startBtn = new Button("Begin Your Reign");
        startBtn.getStyleClass().add("login-submit");
        startBtn.setOnAction(e -> {
            final String name = rectorField.getText().trim();
            final String fac = facultyField.getText().trim();

            if (name.isEmpty() || fac.isEmpty()) {
                errorLabel.setText("Both fields are required to begin.");
                errorLabel.setVisible(true);
                return;
            }
            stage.close();
            onLoginComplete.accept(new PlayerInfo(name, fac));
        });

        facultyField.setOnAction(e -> startBtn.fire());

        final Label title = new Label("Aurea Mediocritas");
        title.getStyleClass().add("login-title");

        final Label subtitle = new Label("Three years of governance await");
        subtitle.getStyleClass().add("login-subtitle");

        final VBox form = new VBox(FORM_SPACING);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(FORM_PADDING));
        form.getChildren().addAll(logo, title, subtitle, rectorField, facultyField, errorLabel, startBtn);
        form.getStyleClass().add("login-form");

        final StackPane root = new StackPane(form);
        applyBackground(root);

        final Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        final var stylesheet = getClass().getResource("/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
    }

    private ImageView loadLogo() {
        try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
            if (Objects.nonNull(is)) {
                final ImageView img = new ImageView(new Image(is));
                img.setFitWidth(LOGO_SIZE);
                img.setFitHeight(LOGO_SIZE);
                img.setPreserveRatio(true);
                return img;
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Logo image could not be loaded", e);
        }
        return new ImageView();
    }

    private void applyBackground(final StackPane root) {
        try (InputStream bgIs = getClass().getResourceAsStream("/background.png")) {
            if (Objects.nonNull(bgIs)) {
                final BackgroundSize coverSize = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO,
                    true, true,
                    false, true
                );
                root.setBackground(new Background(new BackgroundImage(
                    new Image(bgIs),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    coverSize
                )));
            } else {
                root.setBackground(new Background(new BackgroundFill(
                    Color.web("#1a0f08"), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Background image could not be loaded for LoginScene", e);
        }
    }
}
