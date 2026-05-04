package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.GameView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the GameView, with a Medieval / Reigns visual style.
 * 
 * <p>This class is responsible only for the top-level layout (header with parameter
 * icons, central card area, footer with the time label) and for routing model
 * updates to the relevant sub-components ({@link ParameterIconView}, {@link CardPanel}).
 */
public final class GameViewJavaFXImpl implements GameView {

    private static final int SCENE_WIDTH = 500;
    private static final int SCENE_HEIGHT = 850;

    // Recurring colors and styles
    private static final String COLOR_BG_LEATHER = "#1a0f08";
    private static final String COLOR_GOLD_BORDER = "#8b6914";
    private static final String COLOR_TEXT_STORY = "#e8d5b0";
    private static final String COLOR_NAME_GOLD = "#c4a06a";
    private static final String COLOR_TIME = "#a88e6e";
    private static final String CSS_TEXT_FILL = "-fx-text-fill: ";

    // Font fallbacks
    private static final String FONT_STORY = "'IM Fell English', 'Georgia', serif";
    private static final String FONT_UI = "'Cinzel', 'Palatino Linotype', 'Book Antiqua', serif";

    // Layout spacing
    private static final int HEADER_SPACING = 30;
    private static final int TOP_BAR_SPACING = 25;
    private static final int CONTAINER_SPACING = 15;
    private static final int PADDING_LARGE = 30;
    private static final int PADDING_NORMAL = 20;

    // Misc
    private static final int SEMESTERS_PER_YEAR = 2;
    private static final int OFFSET_YEAR = 1;
    private static final double BG_PERCENT = 100.0;
    private static final int TEXT_MIN_HEIGHT = 100;

    private static final Logger LOGGER = Logger.getLogger(GameViewJavaFXImpl.class.getName());

    private it.unibo.aurea.controller.api.GameController controller;

    private final Map<ParameterType, ParameterIconView> parameterIcons = new EnumMap<>(ParameterType.class);

    private CardPanel cardPanel;
    private Label cardMainText;
    private Label characterNameLabel;
    private Label timeLabel;

    /**
     * Constructor for the JavaFX view.
     */
    public GameViewJavaFXImpl() {
        try {
            Platform.startup(() -> { });
        } catch (final IllegalStateException e) {
            LOGGER.log(Level.FINE, "JavaFX Platform already started", e);
        }

        Platform.runLater(this::buildAndShowStage);
    }

    private void buildAndShowStage() {
        final Stage primaryStage = new Stage();
        primaryStage.setTitle("Aurea - The Realm");

        initUiPieces();

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

        final BorderPane root = new BorderPane();
        applyLeatherBackground(root);

        final VBox gameContainer = new VBox(CONTAINER_SPACING);
        gameContainer.setAlignment(Pos.TOP_CENTER);
        gameContainer.setPadding(new Insets(PADDING_NORMAL));
        gameContainer.getChildren().addAll(cardMainText, cardPanel, characterNameLabel);

        final HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(PADDING_NORMAL));
        footer.getChildren().add(timeLabel);

        root.setTop(header);
        root.setCenter(gameContainer);
        root.setBottom(footer);

        final Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void applyLeatherBackground(final BorderPane root) {
        try (InputStream bgIs = getClass().getResourceAsStream("/bg_leather.png")) {
            if (Objects.nonNull(bgIs)) {
                final BackgroundSize bgSize = new BackgroundSize(BG_PERCENT, BG_PERCENT, true, true, true, true);
                root.setBackground(new Background(new BackgroundImage(
                    new Image(bgIs), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                    BackgroundPosition.CENTER, bgSize
                )));
            } else {
                root.setBackground(plainLeatherBackground());
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Could not load leather background, falling back to solid color", e);
            root.setBackground(plainLeatherBackground());
        }
    }

    private static Background plainLeatherBackground() {
        return new Background(new BackgroundFill(
            Color.web(COLOR_BG_LEATHER), CornerRadii.EMPTY, Insets.EMPTY));
    }

    private void initUiPieces() {
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

        this.parameterIcons.put(ParameterType.FINANCES, new ParameterIconView("param_finances.png"));
        this.parameterIcons.put(ParameterType.STUDENTS, new ParameterIconView("param_students.png"));
        this.parameterIcons.put(ParameterType.PROFESSORS, new ParameterIconView("param_professors.png"));
        this.parameterIcons.put(ParameterType.REPUTATION, new ParameterIconView("param_reputation.png"));

        this.cardPanel = new CardPanel();
        this.cardPanel.setPreviewProvider(this::computePreview);
        this.cardPanel.setOnPreviewEnd(this::resetHighlights);
        this.cardPanel.setOnDecision((card, approved) -> {
            if (this.controller != null) {
                this.controller.makeDecision(approved);
            }
        });
    }

    private Set<ParameterType> computePreview(final boolean isApproval) {
        if (this.controller == null) {
            return Set.of();
        }
        final Set<ParameterType> affected = this.controller.previewDecision(isApproval);
        resetHighlights();
        affected.forEach(type -> parameterIcons.get(type).highlight());
        return affected;
    }

    private void resetHighlights() {
        parameterIcons.values().forEach(ParameterIconView::unhighlight);
    }

    @Override
    public void displayCard(final Card card) {
        Platform.runLater(() -> {
            if (card != null) {
                this.cardMainText.setText("« " + card.getDescription() + " »");
                this.characterNameLabel.setText(card.getCharacter().name());
            }
            this.cardPanel.displayCard(card);
        });
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
            this.characterNameLabel.setText("THE END");
            this.cardPanel.clear();
        });
    }

    @Override
    public void showDefeat() {
        Platform.runLater(() -> {
            this.cardMainText.setText("« The realm crumbles to dust. Your reign is over. »");
            this.characterNameLabel.setText("THE END");
            this.cardPanel.clear();
        });
    }

    @Override
    public void showGameOver(final String reason) {
        Platform.runLater(() -> {
            this.cardMainText.setText("« " + reason + " The court has ousted you. »");
            this.characterNameLabel.setText("TRAGIC DEMISE");
            this.cardPanel.clear();
        });
    }
}
