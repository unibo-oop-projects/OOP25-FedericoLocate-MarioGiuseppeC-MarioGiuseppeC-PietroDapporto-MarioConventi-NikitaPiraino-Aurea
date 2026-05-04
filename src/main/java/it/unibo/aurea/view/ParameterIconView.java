package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.ParameterImpl;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A self-contained visual component representing one of the four game parameters.
 * 
 * <p>
 * It displays the parameter icon with a "fill" animation (like a vial filling up)
 * and a small dot above it that lights up when the parameter is about to be affected
 * by the player's pending decision (preview).
 * 
 * <p>
 * This class encapsulates everything related to a single parameter icon, removing
 * the need for parallel data structures (4 {@code DoubleProperty}, 4 {@code Circle},
 * 4 boxes) inside the main view.
 */
public final class ParameterIconView extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(ParameterIconView.class.getName());

    private static final int ICON_SIZE = 65;
    private static final int DOT_RADIUS = 5;
    private static final int DOT_OFFSET_Y = -15;
    private static final double DIMMED_OPACITY = 0.25;
    private static final double FILL_ANIM_MILLIS = 600;
    private static final double PULSE_DURATION_MILLIS = 700;
private static final double PULSE_MIN_OPACITY = 0.4;
    private static final String GOLD_BORDER = "#8b6914";

    private final DoubleProperty fill = new SimpleDoubleProperty(ParameterImpl.START_LEVEL);
    private final Circle dot;
    private final Rectangle clip;
    private Timeline fillAnimation;
    private FadeTransition pulse;

    /**
     * Builds an icon view for a parameter.
     *
     * @param resourceName the file name of the icon (must live in resources root)
     */
    public ParameterIconView(final String resourceName) {
        this.dot = createDot();
        this.clip = new Rectangle(ICON_SIZE, ICON_SIZE);
        loadIcon(resourceName);
        bindFillToClip();
        applyInitialClip();
    }

    private Circle createDot() {
        final Circle circle = new Circle(DOT_RADIUS, Color.web(GOLD_BORDER));
        circle.setOpacity(0);
        return circle;
    }

    private void loadIcon(final String resourceName) {
        try (InputStream is = getClass().getResourceAsStream("/" + resourceName)) {
            if (Objects.isNull(is)) {
                LOGGER.log(Level.WARNING, () -> "Missing icon resource: " + resourceName);
                return;
            }
            final Image image = new Image(is);

            final ImageView dimmed = buildImageView(image);
            dimmed.setOpacity(DIMMED_OPACITY);

            final ImageView active = buildImageView(image);
            active.setClip(clip);

            this.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0.5, 0, 2);");
            setAlignment(dot, Pos.TOP_CENTER);
            dot.setTranslateY(DOT_OFFSET_Y);

            this.getChildren().addAll(dimmed, active, dot);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load parameter icon: " + resourceName, e);
        }
    }

    private ImageView buildImageView(final Image image) {
        final ImageView view = new ImageView(image);
        view.setFitWidth(ICON_SIZE);
        view.setFitHeight(ICON_SIZE);
        view.setPreserveRatio(true);
        return view;
    }

    private void bindFillToClip() {
        fill.addListener((obs, oldV, newV) -> updateClipFromPercentage(newV.doubleValue()));
    }

    private void applyInitialClip() {
        updateClipFromPercentage(ParameterImpl.START_LEVEL);
    }

    private void updateClipFromPercentage(final double percentage) {
        final double normalized = percentage / ParameterImpl.MAX_LEVEL;
        final double filledHeight = ICON_SIZE * normalized;
        clip.setY(ICON_SIZE - filledHeight);
        clip.setHeight(filledHeight);
    }

    /**
     * Sets the parameter level with a smooth fill animation.
     *
     * @param newLevel the new level (0..100)
     */
    public void setLevel(final int newLevel) {
        if (fillAnimation != null) {
            fillAnimation.stop();
        }
        fillAnimation = new Timeline(new KeyFrame(
            Duration.millis(FILL_ANIM_MILLIS),
            new KeyValue(fill, newLevel, Interpolator.EASE_BOTH)
        ));
        fillAnimation.play();
    }

    /**
     * Highlights this icon with a rhythmic pulse to preview a pending decision.
     */
    public void highlight() {
        if (pulse == null) {
            pulse = new FadeTransition(Duration.millis(PULSE_DURATION_MILLIS), dot);
            pulse.setFromValue(PULSE_MIN_OPACITY);
            pulse.setToValue(1.0);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
        }
        pulse.playFromStart();
    }

    /**
     * Removes the highlight (turns off the dot and stops the pulse).
     */
    public void unhighlight() {
        if (pulse != null) {
            pulse.stop();
        }
        dot.setOpacity(0);
    }
}
