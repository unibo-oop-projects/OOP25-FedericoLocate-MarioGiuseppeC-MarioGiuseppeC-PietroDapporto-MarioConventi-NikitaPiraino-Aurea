package it.unibo.aurea.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unibo.aurea.model.ParameterImpl;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
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
 * <p>Displays the parameter icon with a vertical fill indicator and a small
 * white dot above it that lights up when the parameter is about to be affected
 * by the player's pending decision (preview during drag).
 *
 * <p>The dot is sized proportionally to the magnitude of the impact: a large
 * delta (e.g. 12) produces the maximum dot radius, a small delta (e.g. 2)
 * produces the minimum radius.
 */
public final class ParameterIconView extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(ParameterIconView.class.getName());

    private static final int ICON_SIZE = 73;
    private static final int DOT_MIN_RADIUS = 2;
    private static final int DOT_MAX_RADIUS = 5;
    private static final int DOT_REFERENCE_DELTA = 12;
    private static final int DOT_ABOVE_GAP = 11;

    private static final double DIMMED_OPACITY = 0.50;
    private static final double DESATURATE_AMOUNT = -1.0;
    private static final double DIMMED_BRIGHTNESS = -0.3;
    private static final double FILL_ANIM_MILLIS = 350;

    private final DoubleProperty fill = new SimpleDoubleProperty(ParameterImpl.START_LEVEL);
    private Circle previewDot;
    private ImageView active;
    private Timeline fillAnimation;

    /**
     * Builds an icon view for the given resource (e.g. "param_finances.png").
     *
     * @param resourceName file name of the icon, located at the root of resources
     */
    public ParameterIconView(final String resourceName) {
        getStyleClass().add("parameter-icon");
        setPrefSize(ICON_SIZE, ICON_SIZE);
        setMaxSize(ICON_SIZE, ICON_SIZE);
        setMinSize(ICON_SIZE, ICON_SIZE);
        loadIcon(resourceName);
        bindFillToClip();
        applyInitialClip();
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
            final ColorAdjust desaturate = new ColorAdjust();
            desaturate.setSaturation(DESATURATE_AMOUNT);
            desaturate.setBrightness(DIMMED_BRIGHTNESS);
            dimmed.setEffect(desaturate);
            dimmed.setBlendMode(BlendMode.SCREEN);

            this.active = buildImageView(image);
            this.active.setBlendMode(BlendMode.SCREEN);

            this.previewDot = createPreviewDot(DOT_MIN_RADIUS);

            getChildren().addAll(dimmed, this.active, previewDot);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load parameter icon: " + resourceName, e);
        }
    }

    private Circle createPreviewDot(final int radius) {
        final Circle dot = new Circle(radius, Color.WHITE);
        dot.setOpacity(0);
        setAlignment(dot, Pos.TOP_CENTER);
        dot.setTranslateY(-(DOT_ABOVE_GAP + 2 * radius));
        return dot;
    }

    private ImageView buildImageView(final Image image) {
        final ImageView view = new ImageView(image);
        view.setFitWidth(ICON_SIZE);
        view.setFitHeight(ICON_SIZE);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        return view;
    }

    private void bindFillToClip() {
        fill.addListener((obs, oldV, newV) -> updateClipFromPercentage(newV.doubleValue()));
    }

    private void applyInitialClip() {
        updateClipFromPercentage(ParameterImpl.START_LEVEL);
    }

    private void updateClipFromPercentage(final double percentage) {
        if (active == null) {
            return;
        }
        final double normalized = percentage / ParameterImpl.MAX_LEVEL;
        final double filledHeight = ICON_SIZE * normalized;
        final Rectangle newClip = new Rectangle(0, ICON_SIZE - filledHeight, ICON_SIZE, filledHeight);
        active.setClip(newClip);
    }

    /**
     * Sets the parameter level with a smooth fill animation.
     * Snaps instantly to extremes (0 or MAX_LEVEL) to avoid rendering
     * desync that would prevent the game-over visuals from being coherent.
     *
     * @param newLevel the new level (0..100)
     */
    public void setLevel(final int newLevel) {
        if (fillAnimation != null) {
            fillAnimation.stop();
        }
        if (newLevel <= ParameterImpl.MIN_LEVEL || newLevel >= ParameterImpl.MAX_LEVEL) {
            fill.set(newLevel);
            return;
        }
        fillAnimation = new Timeline(new KeyFrame(
            Duration.millis(FILL_ANIM_MILLIS),
            new KeyValue(fill, newLevel, Interpolator.EASE_BOTH)
        ));
        fillAnimation.play();
    }

    /**
     * Marks this icon as "about to be affected" by the
     * pending decision. The preview dot is sized proportionally to the
     * magnitude of the upcoming change.
     *
     * @param affected true to show the dot, false to hide it
     * @param absDelta the absolute value of the delta that the pending
     *                 decision would apply to this parameter (ignored if
     *                 {@code affected} is false)
     */
    public void setAffected(final boolean affected, final int absDelta) {
        if (previewDot == null) {
            return;
        }
        if (!affected) {
            previewDot.setOpacity(0);
            return;
        }
        final int radius = computeDotRadius(absDelta);
        getChildren().remove(previewDot);
        this.previewDot = createPreviewDot(radius);
        previewDot.setOpacity(1.0);
        getChildren().add(previewDot);
    }

    /**
     * Linearly interpolates the dot radius between {@code DOT_MIN_RADIUS}
     * and {@code DOT_MAX_RADIUS}, clamped at {@code DOT_REFERENCE_DELTA}.
     *
     * @param absDelta the absolute delta of the pending impact
     * @return the dot radius in pixels
     */
    private int computeDotRadius(final int absDelta) {
        final double t = Math.min((double) absDelta / DOT_REFERENCE_DELTA, 1.0);
        final double interpolated = DOT_MIN_RADIUS + (DOT_MAX_RADIUS - DOT_MIN_RADIUS) * t;
        return (int) Math.round(interpolated);
    }
}
