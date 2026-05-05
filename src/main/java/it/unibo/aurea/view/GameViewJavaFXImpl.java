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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the GameView, with a Medieval / Reigns visual style.
 * 
 * <p>This class is responsible only for the top-level layout (header with parameter
 * icons, central card area, footer with the time label) and for routing model
 * updates to the relevant sub-components ({@link ParameterIconView}, {@link CardPanel}).
 */
public final class GameViewJavaFXImpl implements GameView {

    private static final int SCENE_WIDTH = 1100;
    private static final int SCENE_HEIGHT = 900;

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
    private static final int PADDING_NORMAL = 20;
    private static final int PARAM_PADDING = 10;
    private static final int INFO_BTN_WIDTH = 48;
    private static final int GAME_COLUMN_WIDTH = 520;
    private static final int BOTTOM_SPACING = 8;
    private static final int PARAMS_OUTER_PADDING = 16;

    // Misc
    private static final int SEMESTERS_PER_YEAR = 2;
    private static final int OFFSET_YEAR = 1;
    private static final int TEXT_MIN_HEIGHT = 100;
    private static final int RULES_DIALOG_WIDTH = 480;
    private static final int RULES_DIALOG_HEIGHT = 420;
    private static final double BOOK_ICON_SCALE = 1.4;

    private static final Logger LOGGER = Logger.getLogger(GameViewJavaFXImpl.class.getName());

    private it.unibo.aurea.controller.api.GameController controller;

    private final Map<ParameterType, ParameterIconView> parameterIcons = new EnumMap<>(ParameterType.class);

    private CardPanel cardPanel;
    private Label cardMainText;
    private Label characterNameLabel;
    private Label timeLabel;
    private EndgameOverlay endgameOverlay;

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

        final HBox topSection = buildTopSection();
        final VBox centerSection = buildCenterSection();
        final VBox bottomSection = buildBottomSection();

        final BorderPane gameColumn = new BorderPane();
        gameColumn.setTop(topSection);
        gameColumn.setCenter(centerSection);
        gameColumn.setBottom(bottomSection);
        gameColumn.setMaxWidth(GAME_COLUMN_WIDTH);
        gameColumn.setMinWidth(GAME_COLUMN_WIDTH);
        gameColumn.setStyle(
            "-fx-background-color: rgba(8, 5, 2, 0.88);"
            + "-fx-background-radius: 12;"
            + "-fx-border-color: " + COLOR_GOLD_BORDER + ";"
            + "-fx-border-width: 2;"
            + "-fx-border-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 25, 0, 0, 0);"
        );

        final BorderPane root = new BorderPane();
        applyBackground(root);
        root.setCenter(gameColumn);
        BorderPane.setMargin(gameColumn, new Insets(PADDING_NORMAL));

        final Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        final var stylesheet = getClass().getResource("/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        } else {
            LOGGER.log(Level.WARNING, "styles.css not found in resources");
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox buildTopSection() {
        final SVGPath bookIcon = new SVGPath();
        bookIcon.setContent("M4 4v16h6c1.1 0 2 .9 2 2 0-1.1.9-2 2-2h6V4h-6c-1.1 0-2 "
            + ".9-2 2 0-1.1-.9-2-2-2H4zm2 2h4c.55 0 1 .45 1 1v11H6V6zm8 0h4v12h-4c-.55 "
            + "0-1-.45-1-1V7c0-.55.45-1 1-1z");
        bookIcon.setFill(Color.web(COLOR_NAME_GOLD));
        bookIcon.setStroke(Color.web(COLOR_NAME_GOLD));
        bookIcon.setScaleX(BOOK_ICON_SCALE);
        bookIcon.setScaleY(BOOK_ICON_SCALE);

        final Button infoBtn = new Button();
        infoBtn.setGraphic(bookIcon);
        infoBtn.getStyleClass().add("info-button");
        infoBtn.setOnAction(e -> showRules());

        final Region rightSpacer = new Region();
        rightSpacer.setMinWidth(INFO_BTN_WIDTH);
        rightSpacer.setMaxWidth(INFO_BTN_WIDTH);

        final HBox parametersGroup = new HBox(TOP_BAR_SPACING);
        parametersGroup.setAlignment(Pos.CENTER);
        parametersGroup.getChildren().addAll(
            parameterIcons.get(ParameterType.FINANCES),
            parameterIcons.get(ParameterType.STUDENTS),
            parameterIcons.get(ParameterType.PROFESSORS),
            parameterIcons.get(ParameterType.REPUTATION)
        );
        HBox.setHgrow(parametersGroup, Priority.ALWAYS);

        final HBox bar = new HBox(HEADER_SPACING);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(PARAM_PADDING));
        bar.setMaxWidth(GAME_COLUMN_WIDTH - 2 * PARAMS_OUTER_PADDING);
        bar.setStyle(
            "-fx-background-color: rgba(40, 28, 14, 0.85);"
            + "-fx-background-radius: 8;"
            + "-fx-border-color: rgba(139, 105, 20, 0.55);"
            + "-fx-border-width: 1;"
            + "-fx-border-radius: 8;"
        );
        bar.getChildren().addAll(infoBtn, parametersGroup, rightSpacer);

        final HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(PADDING_NORMAL, PARAMS_OUTER_PADDING, PADDING_NORMAL / 2, PARAMS_OUTER_PADDING));
        container.getChildren().add(bar);
        return container;
    }

    private VBox buildCenterSection() {
        final VBox center = new VBox(CONTAINER_SPACING);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(PADDING_NORMAL));
        center.getChildren().addAll(cardMainText, cardPanel, characterNameLabel);
        return center;
    }

    private VBox buildBottomSection() {
        final VBox bottom = new VBox(BOTTOM_SPACING);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(PADDING_NORMAL));
        bottom.getChildren().add(timeLabel);
        return bottom;
    }

    private void applyBackground(final BorderPane root) {
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
                root.setBackground(plainLeatherBackground());
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Could not load background image, falling back to solid color", e);
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

        this.endgameOverlay = new EndgameOverlay();
    }

    private Set<ParameterType> computePreview(final boolean isApproval) {
        if (this.controller == null) {
            return Set.of();
        }
        final Set<ParameterType> affected = this.controller.previewDecision(isApproval);
        parameterIcons.forEach((type, icon) -> icon.setAffected(affected.contains(type)));
        return affected;
    }

    private void resetHighlights() {
        parameterIcons.values().forEach(icon -> icon.setAffected(false));
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
        final String body = """
            Welcome, Magnificent Rector.

            Your task is to lead the University through three years of governance,
            balancing the Four Pillars of the Realm:

            FINANCES — the gold that sustains every ambition.
            STUDENTS — those who fill the halls and chant your name.
            PROFESSORS — the masters whose wisdom builds your legacy.
            REPUTATION — the voice of the public, swift to praise and to scorn.

            EACH SEMESTER you will face decisions, presented as cards bearing
            the words of those who depend on you. Swipe RIGHT to accept,
            LEFT to refuse. Each choice will tilt the Pillars.

            ENDGAME
            Your reign ends in glory if you survive the three full years.
            It ends in tragedy if any of the Four Pillars falls to ruin (0)
            or overflows in hubris (100).

            TIPS
            Watch the white dot above each pillar — it warns you which ones
            will be touched by your current decision. Hover with your eye
            to feel the weight of the choice before you make it.

            Your reign begins now.
            """;

        final Label title = new Label("The Royal Decrees");
        title.getStyleClass().add("rules-title");

        final Label content = new Label(body);
        content.setWrapText(true);
        content.getStyleClass().add("rules-body");

        final ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("rules-scroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(RULES_DIALOG_HEIGHT);

        final VBox dialogContent = new VBox(8);
        dialogContent.getStyleClass().add("rules-dialog");
        dialogContent.getChildren().addAll(title, scroll);

        final Stage dialog = new Stage();
        dialog.setTitle("Tome of Decrees");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(dialogContent, RULES_DIALOG_WIDTH, RULES_DIALOG_HEIGHT));
        final var ss = getClass().getResource("/styles.css");
        if (ss != null) {
            dialog.getScene().getStylesheets().add(ss.toExternalForm());
        }
        dialog.showAndWait();
    }

    @Override
    public void setController(final it.unibo.aurea.controller.api.GameController c) {
        this.controller = c;
    }

    @Override
    public void showVictory() {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                "Aurea Mediocritas",
                "The annals shall remember your Golden Age. A true visionary."
            );
        });
    }

    @Override
    public void showDefeat() {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                "The Realm Crumbles",
                "Your reign is over. The university falls into oblivion."
            );
        });
    }

    @Override
    public void showGameOver(final String reason) {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                "Tragic Demise",
                reason + " The court has ousted you."
            );
        });
    }
}
