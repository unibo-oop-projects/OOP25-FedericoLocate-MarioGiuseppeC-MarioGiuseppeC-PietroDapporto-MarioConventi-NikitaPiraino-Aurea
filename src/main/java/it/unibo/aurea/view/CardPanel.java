package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.ParameterType;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Interactive card component including the "physical deck" underlay,
 * the swipeable card on top, the character portrait, and two big "badge"
 * decision labels that appear at the sides during a drag.
 *
 * <p>The labels are rendered as overlay nodes on top of everything (including
 * the game column background), allowing them to extend into the surrounding
 * scene area for a Reigns-style feel.
 */
public final class CardPanel extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(CardPanel.class.getName());

    private static final int CARD_W = 260;
    private static final int CARD_H = 340;
    private static final int CHAR_W = CARD_W;
    private static final int CHAR_H = CARD_H;
    private static final int CORNER_RADIUS = 12;

    private static final int LABEL_MAX_WIDTH = 200;
    private static final int LABEL_OFFSET_X = 200;
    private static final int LABEL_REVEAL_TRAVEL = 30;

    private static final int DECK_LAYER_OFFSET = 4;
    private static final double DECK_LAYER_SCALE = 0.96;
    private static final double DECK_LAYER_OPACITY_STEP = 0.18;
    private static final double FULL_OPACITY = 1.0;

    private static final double DRAG_THRESHOLD = 150.0;
    private static final double DRAG_HINT_THRESHOLD = 15.0;
    private static final double ROTATION_FACTOR = 0.08;
    private static final double HINT_SCALE = 1.02;
    private static final double NORMAL_SCALE = 1.0;
    private static final int FLIGHT_DURATION = 250;
    private static final int SNAP_DURATION = 150;
    private static final int EXIT_X_POS = 1000;
    private static final int ENTER_DURATION = 280;
    private static final double ENTER_START_Y = 60;

    private static final String COLOR_PARCHMENT_DARK = "#d6c39a";
    private static final String COLOR_GOLD_BORDER = "#8b6914";
    private static final String COLOR_APPROVE_TINT = "rgba(39, 174, 96, 0.18)";
    private static final String COLOR_REFUSE_TINT = "rgba(192, 57, 43, 0.18)";
    private static final String CSS_BG_RADIUS = "-fx-background-radius: ";

    private final VBox cardVisual;
    private final StackPane characterSlot;
    private final Region tintOverlay;
    private final Label refusalLabel;
    private final Label approvalLabel;

    private Card currentCard;
    private double dragStartX;
    private boolean hintActive;

    private BiConsumer<Card, Boolean> onDecision = (card, approved) -> { };
    private Function<Boolean, Set<ParameterType>> previewProvider = approved -> Set.of();
    private Runnable onPreviewEnd = () -> { };

    /**
     * Builds the card panel with an empty card and the deck underlay.
     */
    public CardPanel() {
        this.characterSlot = new StackPane();
        this.characterSlot.setPrefSize(CHAR_W, CHAR_H);

        this.tintOverlay = new Region();
        this.tintOverlay.setMouseTransparent(true);
        this.tintOverlay.setOpacity(0);
        this.tintOverlay.setMaxSize(CARD_W, CARD_H);

        this.refusalLabel = buildSideLabel("decision-label-refusal");
        this.approvalLabel = buildSideLabel("decision-label-approval");

        this.cardVisual = buildCardVisual();
        wireDragGestures();

        final VBox deckLayer3 = buildDeckLayer(3);
        final VBox deckLayer2 = buildDeckLayer(2);
        final VBox deckLayer1 = buildDeckLayer(1);

        setAlignment(Pos.CENTER);
        getChildren().addAll(deckLayer3, deckLayer2, deckLayer1, cardVisual,
                             refusalLabel, approvalLabel);

        setAlignment(refusalLabel, Pos.CENTER);
        setAlignment(approvalLabel, Pos.CENTER);
        positionSideLabelsAtRest();
    }

    private Label buildSideLabel(final String variantClass) {
        final Label label = new Label("");
        label.setWrapText(true);
        label.setMaxWidth(LABEL_MAX_WIDTH);
        label.setMinWidth(LABEL_MAX_WIDTH);
        label.setOpacity(0);
        label.setMouseTransparent(true);
        label.getStyleClass().addAll("decision-label", variantClass);
        return label;
    }

    private void positionSideLabelsAtRest() {
        refusalLabel.setTranslateX(-LABEL_OFFSET_X - LABEL_REVEAL_TRAVEL);
        approvalLabel.setTranslateX(LABEL_OFFSET_X + LABEL_REVEAL_TRAVEL);
    }

    private VBox buildCardVisual() {
        final VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(CARD_W, CARD_H);
        card.setStyle(mainCardStyle());

        final StackPane overlay = new StackPane(characterSlot, tintOverlay);
        card.getChildren().add(overlay);
        return card;
    }

    private VBox buildDeckLayer(final int depth) {
        final VBox layer = new VBox();
        layer.setMaxSize(CARD_W, CARD_H);
        layer.setStyle(deckLayerStyle(COLOR_PARCHMENT_DARK));
        layer.setTranslateY((double) -DECK_LAYER_OFFSET * depth);
        final double scale = Math.pow(DECK_LAYER_SCALE, depth);
        layer.setScaleX(scale);
        layer.setScaleY(scale);
        layer.setOpacity(FULL_OPACITY - DECK_LAYER_OPACITY_STEP * (depth - 1));
        return layer;
    }

    private static String deckLayerStyle(final String fill) {
        return "-fx-background-color: " + fill + ";"
            + "-fx-border-color: " + COLOR_GOLD_BORDER + ";"
            + "-fx-border-width: 1;"
            + "-fx-border-radius: " + CORNER_RADIUS + ";"
            + CSS_BG_RADIUS + CORNER_RADIUS + ";"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 10, 0, 0, 5);";
    }

    private static String mainCardStyle() {
        return "-fx-background-color: rgba(60, 50, 35, 0.30);"
            + CSS_BG_RADIUS + CORNER_RADIUS + ";"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 18, 0, 0, 8);";
    }

    private void wireDragGestures() {
        cardVisual.setOnMousePressed(this::handlePress);
        cardVisual.setOnMouseDragged(this::handleDrag);
        cardVisual.setOnMouseReleased(this::handleRelease);
    }

    private void handlePress(final MouseEvent event) {
        this.dragStartX = event.getSceneX();
    }

    private void handleDrag(final MouseEvent event) {
        if (currentCard == null) {
            return;
        }
        final double offsetX = event.getSceneX() - dragStartX;
        cardVisual.setTranslateX(offsetX);
        cardVisual.setRotate(offsetX * ROTATION_FACTOR);

        if (Math.abs(offsetX) > DRAG_HINT_THRESHOLD) {
            if (!hintActive) {
                hintActive = true;
                cardVisual.setScaleX(HINT_SCALE);
                cardVisual.setScaleY(HINT_SCALE);
            }
            showHint(offsetX);
            previewProvider.apply(offsetX > 0);
        } else {
            clearHintState();
            onPreviewEnd.run();
        }
    }

    private void showHint(final double offsetX) {
        final boolean isApproval = offsetX > 0;
        final double intensity = Math.min(Math.abs(offsetX) / DRAG_THRESHOLD, FULL_OPACITY);

        if (isApproval) {
            approvalLabel.setText(currentCard.getApproval().getAnswer());
            approvalLabel.setOpacity(intensity);
            approvalLabel.setTranslateX(LABEL_OFFSET_X + LABEL_REVEAL_TRAVEL * (1 - intensity));
            refusalLabel.setOpacity(0);
            refusalLabel.setTranslateX(-LABEL_OFFSET_X - LABEL_REVEAL_TRAVEL);
        } else {
            refusalLabel.setText(currentCard.getRefusal().getAnswer());
            refusalLabel.setOpacity(intensity);
            refusalLabel.setTranslateX(-LABEL_OFFSET_X - LABEL_REVEAL_TRAVEL * (1 - intensity));
            approvalLabel.setOpacity(0);
            approvalLabel.setTranslateX(LABEL_OFFSET_X + LABEL_REVEAL_TRAVEL);
        }

        tintOverlay.setOpacity(intensity);
        tintOverlay.setStyle(
            CSS_BG_RADIUS + CORNER_RADIUS + ";"
            + "-fx-background-color: " + (isApproval ? COLOR_APPROVE_TINT : COLOR_REFUSE_TINT) + ";"
        );
    }

    private void clearHintState() {
        if (hintActive) {
            hintActive = false;
            cardVisual.setScaleX(NORMAL_SCALE);
            cardVisual.setScaleY(NORMAL_SCALE);
        }
        refusalLabel.setOpacity(0);
        approvalLabel.setOpacity(0);
        positionSideLabelsAtRest();
        tintOverlay.setOpacity(0);
    }

    private void handleRelease(final MouseEvent event) {
        clearHintState();
        onPreviewEnd.run();

        if (currentCard == null) {
            return;
        }
        final double offsetX = event.getSceneX() - dragStartX;
        if (Math.abs(offsetX) > DRAG_THRESHOLD) {
            flyOut(offsetX > 0);
        } else {
            snapBack();
        }
    }

    private void flyOut(final boolean approved) {
        final Card decided = currentCard;
        currentCard = null;

        final TranslateTransition exit = new TranslateTransition(
            Duration.millis(FLIGHT_DURATION), cardVisual);
        exit.setToX(approved ? EXIT_X_POS : -EXIT_X_POS);
        exit.setOnFinished(e -> {
            cardVisual.setTranslateX(0);
            cardVisual.setRotate(0);
            onDecision.accept(decided, approved);
        });
        exit.play();
    }

    private void snapBack() {
        final TranslateTransition back = new TranslateTransition(
            Duration.millis(SNAP_DURATION), cardVisual);
        back.setToX(0);
        cardVisual.setRotate(0);
        back.play();
    }

    /**
     * Displays the given card with its character portrait and animates entrance.
     *
     * @param card the card to show
     */
    public void displayCard(final Card card) {
        this.currentCard = card;
        if (card == null) {
            characterSlot.getChildren().clear();
            return;
        }
        updatePortrait(card.getCharacter());
        animateCardEntrance();
    }

    private void animateCardEntrance() {
        cardVisual.setTranslateX(0);
        cardVisual.setRotate(0);
        cardVisual.setTranslateY(ENTER_START_Y);
        cardVisual.setOpacity(0);

        final TranslateTransition slide = new TranslateTransition(
            Duration.millis(ENTER_DURATION), cardVisual);
        slide.setToY(0);

        final FadeTransition fade = new FadeTransition(
            Duration.millis(ENTER_DURATION), cardVisual);
        fade.setFromValue(0);
        fade.setToValue(FULL_OPACITY);

        new ParallelTransition(slide, fade).play();
    }

    /**
     * Clears the panel (used after game-over screens).
     */
    public void clear() {
        this.currentCard = null;
        characterSlot.getChildren().clear();
        getChildren().clear();
    }

    private void updatePortrait(final CharacterType type) {
        characterSlot.getChildren().clear();
        final String path = type.getImagePath();
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (Objects.isNull(is)) {
                LOGGER.log(Level.WARNING, () -> "Portrait missing: " + path);
                return;
            }
            final ImageView img = new ImageView(new Image(is));
            img.setFitWidth(CHAR_W);
            img.setFitHeight(CHAR_H);
            final Rectangle frame = new Rectangle(CHAR_W, CHAR_H);
            frame.setArcWidth(CORNER_RADIUS * 2.0);
            frame.setArcHeight(CORNER_RADIUS * 2.0);
            img.setClip(frame);
            characterSlot.getChildren().add(img);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Could not load portrait: " + path, e);
        }
    }

    /**
     * Registers a listener invoked when the user makes a decision by swiping.
     *
     * @param listener (card decided, approved) -&gt; action
     */
    public void setOnDecision(final BiConsumer<Card, Boolean> listener) {
        this.onDecision = Objects.requireNonNull(listener);
    }

    /**
     * Registers the function that provides the parameters affected by a
     * pending decision (used to highlight icons during a drag).
     *
     * @param provider boolean isApproval -&gt; affected parameters
     */
    public void setPreviewProvider(final Function<Boolean, Set<ParameterType>> provider) {
        this.previewProvider = Objects.requireNonNull(provider);
    }

    /**
     * Registers a callback invoked when the preview hint is dismissed
     * (drag below threshold or release).
     *
     * @param callback the action to run
     */
    public void setOnPreviewEnd(final Runnable callback) {
        this.onPreviewEnd = Objects.requireNonNull(callback);
    }
}
