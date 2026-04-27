package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.GameView;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX implementation of the GameView, mimicking the Reigns UI/UX.
 */
public final class GameViewJavaFXImpl implements GameView {

    private static final double DRAG_THRESHOLD = 150.0;
    private static final double ROTATION_FACTOR = 0.08;
    private static final double DRAG_HINT_THRESHOLD = 30.0;
    private static final int ICON_SIZE = 70;
    private static final int DOT_RADIUS = 4;
    private static final double DOT_STROKE_WIDTH = 1.2;
    private static final int DOT_OFFSET_Y = -18;
    private static final int CARD_SPACING = 25;
    private static final int TOP_BAR_SPACING = 45;
    private static final int SCENE_WIDTH = 900;
    private static final int SCENE_HEIGHT = 850;
    private static final String DEFAULT_VALUE = "50";

    private static final int PADDING_TOP = 40;
    private static final int PADDING_BOTTOM = 20;
    private static final int CHAR_H = 220;
    private static final int CHAR_W = 220;
    private static final int PORTRAIT_W = 200;
    private static final int TEXT_MAX_W = 260;
    private static final int CARD_MAX_W = 340;
    private static final int CARD_MAX_H = 520;
    private static final int BOX_SPACING = 12;

    private static final int FLIGHT_DURATION = 250;
    private static final int SNAP_DURATION = 150;
    private static final int FADE_DURATION = 400;
    private static final int EXIT_X_POS = 1000;

    private static final Logger LOGGER = Logger.getLogger(GameViewJavaFXImpl.class.getName());

    private Stage stage;
    private it.unibo.aurea.controller.api.GameController controller;
    private Card currentCard;

    private Label financesLabel;
    private Label studentsLabel;
    private Label professorsLabel;
    private Label reputationLabel;
    private Circle finDot;
    private Circle stuDot;
    private Circle proDot;
    private Circle repDot;

    private VBox cardVisual;
    private Label cardMainText;
    private Label decisionHintLabel;
    private StackPane characterPlaceholder;
    private double startX;

    /**
     * Constructor for the JavaFX view.
     */
    public GameViewJavaFXImpl() {
        try {
            Platform.startup(() -> { });
        } catch (final IllegalStateException e) {
            LOGGER.log(Level.FINE, "JavaFX Platform already started", e);
        }

        Platform.runLater(() -> {
            this.stage = new Stage();
            this.stage.setTitle("Aurea - University Reign");

            initLabelsAndDots();

            final VBox financesBox = createParameterBox("param_finances.png", financesLabel, finDot);
            final VBox professorsBox = createParameterBox("param_professors.png", professorsLabel, proDot);
            final VBox studentsBox = createParameterBox("param_students.png", studentsLabel, stuDot);
            final VBox reputationBox = createParameterBox("param_reputation.png", reputationLabel, repDot);

            final HBox topBar = new HBox(TOP_BAR_SPACING);
            topBar.setAlignment(Pos.CENTER);
            topBar.setPadding(new Insets(PADDING_TOP, 0, PADDING_BOTTOM, 0));
            topBar.getChildren().addAll(financesBox, studentsBox, professorsBox, reputationBox);

            setupCard();

            final BorderPane root = new BorderPane();
            final RadialGradient bgGradient = new RadialGradient(
                0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#34495e")),
                new Stop(1, Color.web("#1a1a1a"))
            );
            root.setBackground(new Background(new BackgroundFill(bgGradient, CornerRadii.EMPTY, Insets.EMPTY)));

            root.setTop(topBar);
            root.setCenter(new StackPane(cardVisual));

            final Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            this.stage.setScene(scene);
            this.stage.show();
        });
    }

    private void initLabelsAndDots() {
        this.financesLabel = new Label(DEFAULT_VALUE);
        this.studentsLabel = new Label(DEFAULT_VALUE);
        this.professorsLabel = new Label(DEFAULT_VALUE);
        this.reputationLabel = new Label(DEFAULT_VALUE);
        this.cardMainText = new Label("Loading card...");
        this.decisionHintLabel = new Label("");

        this.finDot = createOracleDot();
        this.stuDot = createOracleDot();
        this.proDot = createOracleDot();
        this.repDot = createOracleDot();
    }

    private Circle createOracleDot() {
        final Circle dot = new Circle(DOT_RADIUS, Color.GOLD);
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(DOT_STROKE_WIDTH);
        dot.setOpacity(0);
        return dot;
    }

    private void setupCard() {
        this.characterPlaceholder = new StackPane();
        this.characterPlaceholder.setPrefSize(CHAR_W, CHAR_H);
        this.characterPlaceholder.setStyle("-fx-background-color: transparent;");

        this.cardMainText.setWrapText(true);
        this.cardMainText.setStyle("-fx-font-size: 19px; -fx-font-family: 'Verdana'; "
                + "-fx-text-alignment: center; -fx-text-fill: #2f3640;");
        this.cardMainText.setMaxWidth(TEXT_MAX_W);

        this.decisionHintLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26px;");

        this.cardVisual = new VBox(CARD_SPACING);
        this.cardVisual.setAlignment(Pos.CENTER);
        this.cardVisual.setPadding(new Insets(PADDING_BOTTOM));
        this.cardVisual.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 25; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");
        this.cardVisual.setMaxSize(CARD_MAX_W, CARD_MAX_H);
        this.cardVisual.getChildren().addAll(decisionHintLabel, characterPlaceholder, cardMainText);

        this.cardVisual.setOnMousePressed(e -> {
            this.startX = e.getSceneX();
        });
        this.cardVisual.setOnMouseDragged(this::handleDrag);
        this.cardVisual.setOnMouseReleased(this::handleRelease);
    }

    private VBox createParameterBox(final String fileName, final Label valLabel, final Circle dot) {
        final VBox container = new VBox(BOX_SPACING);
        container.setAlignment(Pos.CENTER);
        final StackPane iconStack = new StackPane();

        try (InputStream resource = getClass().getResourceAsStream("/" + fileName)) {
            if (Objects.nonNull(resource)) {
                final Image icon = new Image(resource);
                final ImageView imageView = new ImageView(icon);
                imageView.setFitWidth(ICON_SIZE);
                imageView.setFitHeight(ICON_SIZE);
                imageView.setPreserveRatio(true);

                StackPane.setAlignment(dot, Pos.TOP_CENTER);
                dot.setTranslateY(DOT_OFFSET_Y);
                iconStack.getChildren().addAll(imageView, dot);
            } else {
                iconStack.getChildren().add(new Label("?"));
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load image resource: " + fileName, e);
            iconStack.getChildren().add(new Label("?"));
        }

        valLabel.setStyle("-fx-text-fill: #f5f6fa; -fx-font-size: 18px; -fx-font-weight: bold;");
        container.getChildren().addAll(iconStack, valLabel);
        return container;
    }

    private void handleDrag(final javafx.scene.input.MouseEvent event) {
        final double offsetX = event.getSceneX() - startX;
        this.cardVisual.setTranslateX(offsetX);
        this.cardVisual.setRotate(offsetX * ROTATION_FACTOR);

        if (Math.abs(offsetX) > DRAG_HINT_THRESHOLD && currentCard != null) {
            final boolean isRight = offsetX > 0;
            this.decisionHintLabel.setOpacity(Math.min(Math.abs(offsetX) / DRAG_THRESHOLD, 1.0));
            this.decisionHintLabel.setText(isRight ? currentCard.getApproval().getAnswer() 
                : currentCard.getRefusal().getAnswer());
            this.decisionHintLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26px; -fx-text-fill: "
                    + (isRight ? "#44bd32;" : "#e84118;"));
            highlightParameters(isRight);
        } else {
            this.decisionHintLabel.setOpacity(0);
            resetHighlights();
        }
    }

    private void handleRelease(final javafx.scene.input.MouseEvent event) {
        final double offsetX = event.getSceneX() - startX;
        if (Math.abs(offsetX) > DRAG_THRESHOLD && controller != null) {
            final TranslateTransition exit = new TranslateTransition(Duration.millis(FLIGHT_DURATION), cardVisual);
            exit.setToX(offsetX > 0 ? EXIT_X_POS : -EXIT_X_POS);
            exit.setOnFinished(e -> {
                if (this.controller != null) {
                    this.controller.makeDecision(offsetX > 0);
                }
                this.cardVisual.setTranslateX(0);
                this.cardVisual.setRotate(0);
            });
            exit.play();
        } else {
            final TranslateTransition back = new TranslateTransition(Duration.millis(SNAP_DURATION), cardVisual);
            back.setToX(0);
            this.cardVisual.setRotate(0);
            back.play();
        }
        this.decisionHintLabel.setOpacity(0);
        resetHighlights();
    }

    private void highlightParameters(final boolean isApproval) {
        if (this.controller != null) {
            final Set<ParameterType> affected = this.controller.previewDecision(isApproval);
            resetHighlights();
            affected.forEach(type -> {
                switch (type) {
                    case FINANCES -> finDot.setOpacity(1);
                    case STUDENTS -> stuDot.setOpacity(1);
                    case PROFESSORS -> proDot.setOpacity(1);
                    case REPUTATION -> repDot.setOpacity(1);
                }
            });
        }
    }

    private void resetHighlights() {
        if (finDot != null) {
            finDot.setOpacity(0);
            stuDot.setOpacity(0);
            proDot.setOpacity(0);
            repDot.setOpacity(0);
        }
    }

    @Override
    public void displayCard(final Card card) {
        this.currentCard = card;
        Platform.runLater(() -> {
            if (this.cardMainText != null && card != null) {
                this.cardMainText.setText(card.getDescription());
                updateCharacterPortrait(card.getCharacter());
                final FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), cardVisual);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }
        });
    }

    private void updateCharacterPortrait(final CharacterType type) {
        this.characterPlaceholder.getChildren().clear();
        final String path = type.getImagePath();
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (Objects.nonNull(is)) {
                final ImageView img = new ImageView(new Image(is));
                img.setFitWidth(PORTRAIT_W);
                img.setPreserveRatio(true);
                this.characterPlaceholder.getChildren().add(img);
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Portrait image not found: " + path, e);
        }
    }

    @Override
    public void updateParameters(final int f, final int s, final int p, final int r) {
        Platform.runLater(() -> {
            if (this.financesLabel != null) {
                this.financesLabel.setText(String.valueOf(f));
                this.studentsLabel.setText(String.valueOf(s));
                this.professorsLabel.setText(String.valueOf(p));
                this.reputationLabel.setText(String.valueOf(r));
            }
        });
    }

    @Override
    public void setController(final it.unibo.aurea.controller.api.GameController c) {
        this.controller = c;
    }

    @Override
    public void showVictory() {
        Platform.runLater(() -> this.cardMainText.setText("VICTORY!"));
    }

    @Override
    public void showDefeat() {
        Platform.runLater(() -> this.cardMainText.setText("DEFEAT!"));
    }

    @Override
    public void showGameOver(final String reason) {
        Platform.runLater(() -> this.cardMainText.setText("GAME OVER:\n" + reason));
    }
}
