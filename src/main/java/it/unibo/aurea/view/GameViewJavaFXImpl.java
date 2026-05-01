package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.GameView;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX implementation of the GameView, utilizing a Medieval / Reigns visual style.
 */
public final class GameViewJavaFXImpl implements GameView {

    // Drag & Drop
    private static final double DRAG_THRESHOLD = 150.0;
    private static final double ROTATION_FACTOR = 0.08;
    private static final double DRAG_HINT_THRESHOLD = 30.0;

    // Costanti per le dimensioni
    private static final int SCENE_WIDTH = 500;
    private static final int SCENE_HEIGHT = 850;
    private static final int CHAR_H = 340;
    private static final int CHAR_W = 280;
    private static final int CARD_MAX_W = 300;
    private static final int CARD_MAX_H = 400;
    private static final int DECK_OFFSET = 8;

    // Colori e stili ricorrenti
    private static final String COLOR_BG_LEATHER = "#1a0f08";
    private static final String COLOR_PARCHMENT = "#f5e8c8";
    private static final String COLOR_PARCHMENT_DARK = "#d6c39a";
    private static final String COLOR_GOLD_BORDER = "#8b6914";
    private static final String COLOR_TEXT_STORY = "#e8d5b0";
    private static final String COLOR_NAME_GOLD = "#c4a06a";
    private static final String COLOR_TIME = "#a88e6e";
    private static final String CSS_TEXT_FILL = "-fx-text-fill: ";

    // Font Fallback
    private static final String FONT_STORY = "'IM Fell English', 'Georgia', serif";
    private static final String FONT_UI = "'Cinzel', 'Palatino Linotype', 'Book Antiqua', serif";

    // Costanti grafiche
    private static final int HINT_OFFSET_Y = -50;
    private static final int CORNER_RADIUS = 6;
    private static final int HEADER_SPACING = 30;
    private static final int TOP_BAR_SPACING = 25;
    private static final int CONTAINER_SPACING = 15;
    private static final int PADDING_LARGE = 30;
    private static final int PADDING_NORMAL = 20;
    private static final int STAR_PADDING = 5;

    // Costanti per Animazioni e Logica
    private static final int FLIGHT_DURATION = 250;
    private static final int SNAP_DURATION = 150;
    private static final int EXIT_X_POS = 1000;
    private static final int SEMESTERS_PER_YEAR = 2;
    private static final int OFFSET_YEAR = 1;
    private static final double BG_PERCENT = 100.0;
    private static final int TEXT_MIN_HEIGHT = 100;

    private static final Logger LOGGER = Logger.getLogger(GameViewJavaFXImpl.class.getName());

    private Stage stage;
    private it.unibo.aurea.controller.api.GameController controller;
    private Card currentCard;

    private final Map<ParameterType, ParameterIconView> parameterIcons = new EnumMap<>(ParameterType.class);

    private StackPane physicalDeck;
    private VBox cardVisual;
    private Label cardMainText;
    private Label characterNameLabel;
    private Label decisionHintLabel;
    private Label timeLabel;
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
            this.stage.setTitle("Aurea - The Realm");

            initLabelsAndDots();

            final HBox topBar = new HBox(TOP_BAR_SPACING);
            topBar.setAlignment(Pos.CENTER);
            topBar.getChildren().addAll(
                parameterIcons.get(ParameterType.FINANCES),
                parameterIcons.get(ParameterType.STUDENTS),
                parameterIcons.get(ParameterType.PROFESSORS),
                parameterIcons.get(ParameterType.REPUTATION)
            );

            final Button infoBtn = new Button("✦");
            infoBtn.setStyle("-fx-background-color: transparent; " + CSS_TEXT_FILL + COLOR_GOLD_BORDER + "; "
                    + "-fx-font-size: 24px; -fx-cursor: hand;");
            infoBtn.setOnAction(e -> showRules());

            final HBox header = new HBox(HEADER_SPACING);
            header.setAlignment(Pos.CENTER);
            header.setPadding(new Insets(PADDING_LARGE, PADDING_NORMAL, PADDING_NORMAL, PADDING_NORMAL));
            header.getChildren().addAll(infoBtn, topBar);

            setupCard();

            final BorderPane root = new BorderPane();

            try (InputStream bgIs = getClass().getResourceAsStream("/bg_leather.png")) {
                if (Objects.nonNull(bgIs)) {
                    final BackgroundSize bgSize = new BackgroundSize(BG_PERCENT, BG_PERCENT, true, true, true, true);
                    root.setBackground(new Background(new BackgroundImage(
                        new Image(bgIs), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                        BackgroundPosition.CENTER, bgSize
                    )));
                } else {
                    root.setBackground(new Background(new BackgroundFill(Color.web(COLOR_BG_LEATHER),
                        CornerRadii.EMPTY, Insets.EMPTY)));
                }
            } catch (final IOException e) {
                root.setBackground(new Background(new BackgroundFill(Color.web(COLOR_BG_LEATHER),
                    CornerRadii.EMPTY, Insets.EMPTY)));
            }

            final VBox gameContainer = new VBox(CONTAINER_SPACING);
            gameContainer.setAlignment(Pos.TOP_CENTER);
            gameContainer.setPadding(new Insets(PADDING_NORMAL));
            gameContainer.getChildren().addAll(cardMainText, physicalDeck, characterNameLabel);

            final HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER);
            footer.setPadding(new Insets(PADDING_NORMAL));
            footer.getChildren().add(timeLabel);

            root.setTop(header);
            root.setCenter(gameContainer);
            root.setBottom(footer);

            final Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            this.stage.setScene(scene);
            this.stage.show();
        });
    }

    private void initLabelsAndDots() {
        this.cardMainText = new Label("The court awaits...");
        this.cardMainText.setWrapText(true);
        this.cardMainText.setTextAlignment(TextAlignment.CENTER);
        this.cardMainText.setStyle("-fx-font-size: 22px; -fx-font-family: " + FONT_STORY + "; "
                + CSS_TEXT_FILL + COLOR_TEXT_STORY + "; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 5, 0.8, 0, 0);");
        this.cardMainText.setMinHeight(TEXT_MIN_HEIGHT);
        this.cardMainText.setAlignment(Pos.BOTTOM_CENTER);

        this.characterNameLabel = new Label("");
        this.characterNameLabel.setStyle("-fx-font-size: 20px; -fx-font-family: " + FONT_UI + "; "
                + "-fx-font-weight: bold; " + CSS_TEXT_FILL + COLOR_NAME_GOLD + "; "
                + "-fx-effect: dropshadow(gaussian, black, 4, 0.5, 0, 0);");

        this.timeLabel = new Label("Year I · Session I");
        this.timeLabel.setStyle("-fx-font-size: 18px; -fx-font-family: " + FONT_UI + "; "
                + CSS_TEXT_FILL + COLOR_TIME + ";");

        this.decisionHintLabel = new Label("");

        this.parameterIcons.put(ParameterType.FINANCES, new ParameterIconView("param_finances.png"));
        this.parameterIcons.put(ParameterType.STUDENTS, new ParameterIconView("param_students.png"));
        this.parameterIcons.put(ParameterType.PROFESSORS, new ParameterIconView("param_professors.png"));
        this.parameterIcons.put(ParameterType.REPUTATION, new ParameterIconView("param_reputation.png"));
    }

    private void setupCard() {
        this.characterPlaceholder = new StackPane();
        this.characterPlaceholder.setPrefSize(CHAR_W, CHAR_H);

        this.decisionHintLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26px; "
                + "-fx-font-family: " + FONT_STORY + ";");

        this.cardVisual = new VBox();
        this.cardVisual.setAlignment(Pos.CENTER);
        this.cardVisual.setStyle("-fx-background-color: " + COLOR_PARCHMENT + "; "
                + "-fx-border-color: " + COLOR_GOLD_BORDER + "; -fx-border-width: 2; "
                + "-fx-border-radius: " + CORNER_RADIUS + "; -fx-background-radius: " + CORNER_RADIUS + "; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);");
        this.cardVisual.setMaxSize(CARD_MAX_W, CARD_MAX_H);

        final Label topLeft = createCornerStar();
        final Label topRight = createCornerStar();
        final Label botLeft = createCornerStar();
        final Label botRight = createCornerStar();
        StackPane.setAlignment(topLeft, Pos.TOP_LEFT);
        StackPane.setAlignment(topRight, Pos.TOP_RIGHT);
        StackPane.setAlignment(botLeft, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(botRight, Pos.BOTTOM_RIGHT);

        final StackPane overlay = new StackPane(characterPlaceholder, decisionHintLabel,
                topLeft, topRight, botLeft, botRight);
        StackPane.setAlignment(decisionHintLabel, Pos.TOP_CENTER);
        decisionHintLabel.setTranslateY(HINT_OFFSET_Y);

        this.cardVisual.getChildren().add(overlay);

        this.cardVisual.setOnMousePressed(e -> this.startX = e.getSceneX());
        this.cardVisual.setOnMouseDragged(this::handleDrag);
        this.cardVisual.setOnMouseReleased(this::handleRelease);

        final VBox deckUnderlay = new VBox();
        deckUnderlay.setStyle("-fx-background-color: " + COLOR_PARCHMENT_DARK + "; "
                + "-fx-border-color: " + COLOR_GOLD_BORDER + "; -fx-border-width: 2; "
                + "-fx-border-radius: " + CORNER_RADIUS + "; -fx-background-radius: " + CORNER_RADIUS + "; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        deckUnderlay.setMaxSize(CARD_MAX_W, CARD_MAX_H);
        deckUnderlay.setTranslateY(DECK_OFFSET);

        this.physicalDeck = new StackPane(deckUnderlay, cardVisual);
        this.physicalDeck.setAlignment(Pos.TOP_CENTER);
    }

    private Label createCornerStar() {
        final Label star = new Label("✦");
        star.setStyle(CSS_TEXT_FILL + COLOR_GOLD_BORDER + "; -fx-font-size: 16px;");
        star.setPadding(new Insets(STAR_PADDING));
        return star;
    }

    private void handleDrag(final javafx.scene.input.MouseEvent event) {
        final double offsetX = event.getSceneX() - startX;
        this.cardVisual.setTranslateX(offsetX);
        this.cardVisual.setRotate(offsetX * ROTATION_FACTOR);

        if (Math.abs(offsetX) > DRAG_HINT_THRESHOLD && currentCard != null) {
            final boolean isRight = offsetX > 0;
            this.decisionHintLabel.setOpacity(Math.min(Math.abs(offsetX) / DRAG_THRESHOLD, 1.0));

            final String answer = isRight ? "✓ " + currentCard.getApproval().getAnswer()
                                          : "✗ " + currentCard.getRefusal().getAnswer();
            this.decisionHintLabel.setText(answer);

            this.decisionHintLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26px; "
                    + "-fx-font-family: " + FONT_STORY + "; "
                    + CSS_TEXT_FILL + (isRight ? "#27ae60;" : "#c0392b;")
                    + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0.8, 0, 0);");
            highlightParameters(isRight);
        } else {
            this.decisionHintLabel.setOpacity(0);
            resetHighlights();
        }
    }

    private void handleRelease(final javafx.scene.input.MouseEvent event) {
        final double offsetX = event.getSceneX() - startX;
        if (Math.abs(offsetX) > DRAG_THRESHOLD && this.controller != null) {
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
    if (this.controller == null) {
        return;
    }
    resetHighlights();
    this.controller.previewDecision(isApproval)
        .forEach(type -> parameterIcons.get(type).highlight());
}

    private void resetHighlights() {
        parameterIcons.values().forEach(ParameterIconView::unhighlight);
    }

    @Override
    public void displayCard(final Card card) {
        this.currentCard = card;
        Platform.runLater(() -> {
            if (card != null) {
                this.cardMainText.setText("« " + card.getDescription() + " »");
                this.characterNameLabel.setText(card.getCharacter().name());
                updateCharacterPortrait(card.getCharacter());
            }
        });
    }

    private void updateCharacterPortrait(final CharacterType type) {
        this.characterPlaceholder.getChildren().clear();
        final String path = type.getImagePath();
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (Objects.nonNull(is)) {
                final ImageView img = new ImageView(new Image(is));
                img.setFitWidth(CHAR_W);
                img.setFitHeight(CHAR_H);

                final Rectangle frame = new Rectangle(CHAR_W, CHAR_H);
                frame.setArcWidth(CORNER_RADIUS);
                frame.setArcHeight(CORNER_RADIUS);
                img.setClip(frame);

                this.characterPlaceholder.getChildren().add(img);
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Portrait missing: " + path, e);
        }
    }

    @Override
    public void updateSingleParameter(final ParameterType type, final int newValue) {
        Platform.runLater(() -> parameterIcons.get(type).setLevel(newValue));
    }

    @Override
    public void updateTime(final int semester, final int turn) {
        Platform.runLater(() -> {
            final int year = (semester / SEMESTERS_PER_YEAR) + OFFSET_YEAR;
            final int visualSession = (semester % SEMESTERS_PER_YEAR) + OFFSET_YEAR;
            this.timeLabel.setText("Year " + toRoman(year) + " · Session " + toRoman(visualSession));
        });
    }

    private String toRoman(final int number) {
        final String[] rom = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return number <= 10 && number > 0 ? rom[number] : String.valueOf(number);
    }

    private void showRules() {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tome of Decrees");
        alert.setHeaderText("The Royal Decrees");
        alert.setContentText("""
                             Swipe LEFT or RIGHT to utter your ruling.
                             Govern the four pillars (Gold, Scholars, Masters, Glory).
                             Let not a single pillar fall to ruin (0), nor let it overflow in hubris (100).
                             Your reign begins now.""");
        alert.showAndWait();
    }

    @Override
    public void setController(final it.unibo.aurea.controller.api.GameController c) {
        this.controller = c;
    }

    @Override
    public void showVictory() {
        Platform.runLater(() -> {
            this.cardMainText.setText("« The annals shall remember your Golden Age. A true visionary. »");
            this.characterPlaceholder.getChildren().clear();
            this.characterNameLabel.setText("THE END");
            this.physicalDeck.getChildren().clear();
        });
    }

    @Override
    public void showDefeat() {
        Platform.runLater(() -> {
            this.cardMainText.setText("« The realm crumbles to dust. Your reign is over. »");
            this.characterPlaceholder.getChildren().clear();
            this.characterNameLabel.setText("THE END");
            this.physicalDeck.getChildren().clear();
        });
    }

    @Override
    public void showGameOver(final String reason) {
        Platform.runLater(() -> {
            this.cardMainText.setText("« " + reason + " The court has ousted you. »");
            this.characterPlaceholder.getChildren().clear();
            this.characterNameLabel.setText("TRAGIC DEMISE");
            this.physicalDeck.getChildren().clear();
        });
    }
}
