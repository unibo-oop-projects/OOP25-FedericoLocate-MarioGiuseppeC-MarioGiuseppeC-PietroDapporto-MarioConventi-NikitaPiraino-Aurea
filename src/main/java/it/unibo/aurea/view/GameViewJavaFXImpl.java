package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.controller.api.PlayerInfo;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFX implementation of the GameView with a Medieval / Reigns visual style.
 *
 * <p>Top-level layout (header with parameter icons, central card area, footer with
 * the time label and player info) and routing of model updates to the relevant
 * sub-components ({@link ParameterIconView}, {@link CardPanel}, {@link EndgameOverlay}).
 *
 * <p>Player identity is read from the controller via {@link GameController#getPlayerInfo()}
 * once the controller is wired in by {@link #setController}.
 */
public final class GameViewJavaFXImpl implements GameView {

    private static final int SCENE_WIDTH = 1100;
    private static final int SCENE_HEIGHT = 900;
    private static final int MIN_WIDTH = 900;
    private static final int MIN_HEIGHT = 700;

    private static final int LOW_THRESHOLD = 10;
    private static final int HIGH_THRESHOLD = 90;

    private static final String COLOR_BG_LEATHER = "#1a0f08";
    private static final String COLOR_NAME_GOLD = "#c4a06a";
    private static final String COLOR_CLOSE_RED = "#c4584a";

    private static final int TOP_BAR_SPACING = 28;
    private static final int CONTAINER_SPACING = 10;
    private static final int PADDING_NORMAL = 16;
    private static final int PARAM_PADDING = 14;
    private static final int GAME_COLUMN_WIDTH = 480;
    private static final int PARAMS_OUTER_PADDING = 14;
    private static final int CLOSE_ROW_PADDING = 8;
    private static final int FOOTER_SPACING = 3;
    private static final int TEXT_MIN_HEIGHT = 70;

    private static final int RULES_POPUP_WIDTH = 440;
    private static final int RULES_POPUP_HEIGHT = 380;

    private static final double INFO_ICON_SCALE = 1.6;
    private static final double CLOSE_ICON_SCALE = 1.3;
    private static final double CLOSE_ICON_STROKE = 2.5;

    private static final int SEMESTERS_PER_YEAR = 2;
    private static final int OFFSET_YEAR = 1;

    private static final Logger LOGGER = Logger.getLogger(GameViewJavaFXImpl.class.getName());

    private final Map<ParameterType, Boolean> warnedLow = new EnumMap<>(ParameterType.class);
    private final Map<ParameterType, Boolean> warnedHigh = new EnumMap<>(ParameterType.class); 

    private GameController controller;
    private final Map<ParameterType, ParameterIconView> parameterIcons = new EnumMap<>(ParameterType.class);

    private CardPanel cardPanel;
    private Label cardMainText;
    private Label characterNameLabel;
    private Label timeLabel;
    private Label playerLabel;
    private EndgameOverlay endgameOverlay;
    private ReportImpl semesterReport;
    private final Runnable onRestart;

    /**
     * Constructor for the JavaFX view.
     *
     * @param onRestart callback invoked when the player chooses to restart
     */
    public GameViewJavaFXImpl(final Runnable onRestart) {
        this.onRestart = Objects.requireNonNull(onRestart);
        for (final ParameterType t : ParameterType.values()) {
            warnedLow.put(t, false);
            warnedHigh.put(t, false);
        }
        Platform.runLater(this::buildAndShowStage);
    }

    private void buildAndShowStage() {
        final Stage primaryStage = new Stage();
        primaryStage.setTitle("Aurea - The Realm");
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        initUiPieces();

        final VBox topSection = buildTopSection();
        final VBox centerSection = buildCenterSection();
        final HBox bottomSection = buildBottomSection();

        final BorderPane gameColumn = new BorderPane();
        gameColumn.setTop(topSection);
        gameColumn.setCenter(centerSection);
        gameColumn.setBottom(bottomSection);
        gameColumn.setMaxWidth(GAME_COLUMN_WIDTH);
        gameColumn.setMinWidth(GAME_COLUMN_WIDTH);
        gameColumn.getStyleClass().add("game-column");

        final BorderPane root = new BorderPane();
        applyBackground(root);
        root.setCenter(gameColumn);
        BorderPane.setMargin(gameColumn, new Insets(PADDING_NORMAL));

        final StackPane sceneRoot = new StackPane(root, semesterReport);

        final Scene scene = new Scene(sceneRoot, SCENE_WIDTH, SCENE_HEIGHT);
        final var stylesheet = getClass().getResource("/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        } else {
            LOGGER.log(Level.WARNING, "styles.css not found in resources");
        }
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private VBox buildTopSection() {
        final Button closeBtn = buildCloseButton();
        final HBox closeRow = new HBox(closeBtn);
        closeRow.setPadding(new Insets(CLOSE_ROW_PADDING));
        closeRow.setAlignment(Pos.CENTER_LEFT);

        final HBox parametersGroup = new HBox(TOP_BAR_SPACING);
        parametersGroup.setAlignment(Pos.CENTER);
        //getChildren is a javaFX method used to return a list where we can add components. 
        //we don't set the position because javaFX does it autonomously by the layout settings.
        parametersGroup.getChildren().addAll(
            parameterIcons.get(ParameterType.FINANCES),
            parameterIcons.get(ParameterType.STUDENTS),
            parameterIcons.get(ParameterType.PROFESSORS),
            parameterIcons.get(ParameterType.REPUTATION)
        );
        HBox.setHgrow(parametersGroup, Priority.ALWAYS);

        final HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(PARAM_PADDING));
        bar.setMaxWidth(GAME_COLUMN_WIDTH - 2 * PARAMS_OUTER_PADDING);
        bar.getStyleClass().add("params-bar");
        bar.getChildren().add(parametersGroup);

        final HBox barWrapper = new HBox();
        barWrapper.setAlignment(Pos.CENTER);
        barWrapper.setPadding(new Insets(0, PARAMS_OUTER_PADDING, PADDING_NORMAL / 2, PARAMS_OUTER_PADDING));
        barWrapper.setMaxWidth(GAME_COLUMN_WIDTH);
        barWrapper.getChildren().add(bar);

        final VBox top = new VBox(FOOTER_SPACING);
        top.setMaxWidth(GAME_COLUMN_WIDTH);
        top.getChildren().addAll(closeRow, barWrapper);
        return top;
    }

    private Button buildCloseButton() {
        final SVGPath xIcon = new SVGPath();
        xIcon.setContent("M6 6L18 18M6 18L18 6");
        xIcon.setStroke(Color.web(COLOR_CLOSE_RED));
        xIcon.setFill(null);
        xIcon.setStrokeWidth(CLOSE_ICON_STROKE);
        xIcon.setScaleX(CLOSE_ICON_SCALE);
        xIcon.setScaleY(CLOSE_ICON_SCALE);

        final Button btn = new Button();
        btn.setGraphic(xIcon);
        btn.getStyleClass().add("close-button");
        btn.setOnAction(e -> {
            if (this.controller != null) {
                this.controller.quitGame();
            }
            Platform.exit();
        });
        return btn;
    }

    private Button buildInfoButton() {
        final SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"
        + "m1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setFill(Color.web(COLOR_NAME_GOLD));
        infoIcon.setScaleX(INFO_ICON_SCALE);
        infoIcon.setScaleY(INFO_ICON_SCALE);

        final Button btn = new Button();
        btn.setGraphic(infoIcon);
        btn.getStyleClass().add("info-button");
        btn.setOnAction(e -> showRules());
        return btn;
    }

    private VBox buildCenterSection() {
        final VBox center = new VBox(CONTAINER_SPACING);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(PADDING_NORMAL));
        center.getChildren().addAll(cardMainText, cardPanel, characterNameLabel);
        return center;
    }

    private HBox buildBottomSection() {
        final VBox leftInfo = new VBox(FOOTER_SPACING);
        leftInfo.setAlignment(Pos.CENTER_LEFT);
        leftInfo.getChildren().addAll(playerLabel, timeLabel);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final Button infoBtn = buildInfoButton();

        final HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(PADDING_NORMAL / 2, PADDING_NORMAL, PADDING_NORMAL, PADDING_NORMAL));
        footer.getChildren().addAll(leftInfo, spacer, infoBtn);
        return footer;
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
        this.cardMainText.getStyleClass().add("card-description");
        this.cardMainText.setMinHeight(TEXT_MIN_HEIGHT);
        this.cardMainText.setAlignment(Pos.BOTTOM_CENTER);

        this.characterNameLabel = new Label("");
        this.characterNameLabel.getStyleClass().add("character-name");

        this.timeLabel = new Label("Year I · Semester I");
        this.timeLabel.getStyleClass().add("time-label");

        this.playerLabel = new Label("");
        this.playerLabel.getStyleClass().add("player-label");

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

        this.semesterReport = new ReportImpl();
        this.endgameOverlay = new EndgameOverlay(this::handleRestart);
    }

    private Set<ParameterType> computePreview(final boolean isApproval) {
        if (this.controller == null) {
            return Set.of();
        }
        final Map<ParameterType, Integer> deltas = this.controller.previewDecisionDeltas(isApproval);
        parameterIcons.forEach((type, icon) -> {
            if (deltas.containsKey(type)) {
                icon.setAffected(true, deltas.get(type));
            } else {
                icon.setAffected(false, 0);
            }
        });
        return deltas.keySet();
    }

    private void resetHighlights() {
        parameterIcons.values().forEach(icon -> icon.setAffected(false, 0));
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
        Platform.runLater(() -> {
            parameterIcons.get(type).setLevel(newValue);
            checkCounsellorThreshold(type, newValue);
        });
    }

    private void checkCounsellorThreshold(final ParameterType type, final int newValue) {
        if (newValue <= LOW_THRESHOLD && !warnedLow.get(type)) {
            warnedLow.put(type, true);
            CounsellorPopup.show(buildLowMessage(type, newValue));
        } else if (newValue > LOW_THRESHOLD && newValue < HIGH_THRESHOLD) {
            warnedLow.put(type, false);
            warnedHigh.put(type, false);
        } else if (newValue >= HIGH_THRESHOLD && !warnedHigh.get(type)) {
            warnedHigh.put(type, true);
            CounsellorPopup.show(buildHighMessage(type, newValue));
        }
    }

    private String buildLowMessage(final ParameterType type, final int value) {
        final String name = currentRectorName();
        return "Magnificent " + name + ", "
            + type.getDisplayName().toLowerCase(Locale.ROOT) + " has fallen to " + value + ". "
            + "Tread carefully — let it not crumble to ruin.";
    }

    private String buildHighMessage(final ParameterType type, final int value) {
        final String name = currentRectorName();
        return "Magnificent " + name + ", "
            + type.getDisplayName().toLowerCase(Locale.ROOT) + " has risen to " + value + ". "
            + "Beware: too much of a good thing breeds new troubles.";
    }

    private String currentRectorName() {
        return controller != null ? controller.getPlayerInfo().rectorName() : "rector";
    }

    @Override
    public void updateTime(final int semester, final int turn) {
        Platform.runLater(() -> {
            final int year = (semester / SEMESTERS_PER_YEAR) + OFFSET_YEAR;
            final int visualSemester = (semester % SEMESTERS_PER_YEAR) + OFFSET_YEAR;
            final String semesterLabel = "Year " + toRoman(year) + " · Semester " + toRoman(visualSemester);
            this.timeLabel.setText(semesterLabel);
            if (turn == 0 && semester > 0) {
                final int prevSemester = semester - 1;
                final int prevYear = (prevSemester / SEMESTERS_PER_YEAR) + OFFSET_YEAR;
                final int prevVisualSemester = (prevSemester % SEMESTERS_PER_YEAR) + OFFSET_YEAR;
                final String prevLabel = "Year " + toRoman(prevYear)
                    + " · Semester " + toRoman(prevVisualSemester);
                semesterReport.show(prevLabel, buildFinalRecap());
            }
                    });
    }

    private String toRoman(final int number) {
        final String[] rom = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return number <= 10 && number > 0 ? rom[number] : String.valueOf(number);
    }

    private void showRules() {
        final Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Tome of Decrees");
        popup.setResizable(false);

        final Label title = new Label("The Royal Decrees");
        title.getStyleClass().add("rules-title");

        final Label body = new Label(rulesText());
        body.setWrapText(true);
        body.getStyleClass().add("rules-body");

        final ScrollPane scroll = new ScrollPane(body);
        scroll.getStyleClass().add("rules-scroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(RULES_POPUP_HEIGHT - 100);

        final Button closeBtn = new Button("Close the Tome");
        closeBtn.getStyleClass().add("counsellor-dismiss");
        closeBtn.setOnAction(e -> popup.close());

        final VBox content = new VBox(12);
        content.setPadding(new Insets(PADDING_NORMAL));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("counsellor-content");
        content.getChildren().addAll(title, scroll, closeBtn);

        final Scene scene = new Scene(content, RULES_POPUP_WIDTH, RULES_POPUP_HEIGHT);
        final var ss = getClass().getResource("/styles.css");
        if (ss != null) {
            scene.getStylesheets().add(ss.toExternalForm());
        }
        popup.setScene(scene);
        popup.showAndWait();
    }

    private static String rulesText() {
        return """
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
    }

    @Override
    public void setController(final GameController c) {
        this.controller = c;
        if (c != null) {
            final PlayerInfo info = c.getPlayerInfo();
            Platform.runLater(() -> {
            if (this.playerLabel != null) {
                this.playerLabel.setText(info.rectorName() + " · " + info.faculty());
            } else {
                LOGGER.warning("playerLabel was null when setController fired");
            }
            });
        }
    }

    @Override
    public void showVictory() {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                this.semesterReport,
                "Aurea Mediocritas",
                "The annals shall remember your Golden Age. A true visionary.",
                buildFinalRecap(),
                true
            );
        });
    }

    @Override
    public void showDefeat() {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                this.semesterReport,
                "The Realm Crumbles",
                "Your reign is over. The university falls into oblivion.",
                buildFinalRecap(),
                false
            );
        });
    }

    @Override
    public void showGameOver(final String reason) {
        Platform.runLater(() -> {
            this.cardPanel.clear();
            this.endgameOverlay.reveal(
                this.semesterReport,
                "Tragic Demise",
                reason + " The court has ousted you.",
                buildFinalRecap(),
                false
            );
        });
    }

    /**
     * Builds the final recap of all four parameters by reading the current
     * levels from the controller. Used by the endgame overlay.
     *
     * @return a map of each parameter type to its final value, or an empty map
     *         if the controller is not yet wired
     */
    private Map<ParameterType, Integer> buildFinalRecap() {
        if (controller == null) {
            return Map.of();
        }
        return controller.getCurrentParametersLevels();
    }

    /**
     * Closes the current game stage and delegates the restart to the owner
     * (Main), which will open a new LoginScene. The view does not know how
     * to restart — it only signals the intent via the callback.
     */
    private void handleRestart() {
        Stage.getWindows().stream()
            .filter(w -> w instanceof Stage)
            .map(w -> (Stage) w)
            .toList()
            .forEach(Stage::close);
        onRestart.run();
    }
}
