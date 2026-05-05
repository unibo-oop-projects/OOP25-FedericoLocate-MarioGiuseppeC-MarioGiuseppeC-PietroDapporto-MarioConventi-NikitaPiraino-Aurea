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
 * <p>Displays the parameter icon with a vertical "fill" indicator and a small
 * white dot above it that lights up when the parameter is about to be affected
 * by the player's pending decision (preview during drag).
 *
 * <p>Visual styling (drop shadow, hover glow) is delegated to the external
 * stylesheet via the {@code parameter-icon} style class.
 */
public final class ParameterIconView extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(ParameterIconView.class.getName());

    private static final int ICON_SIZE = 90;
    private static final int DOT_RADIUS = 3;
    private static final int DOT_OFFSET_Y = -8;
    private static final double DIMMED_OPACITY = 0.55;
    private static final double DESATURATE_AMOUNT = -1.0;
    private static final double DIMMED_BRIGHTNESS = -0.4;
    private static final double FILL_ANIM_MILLIS = 350;

    private final DoubleProperty fill = new SimpleDoubleProperty(ParameterImpl.START_LEVEL);
    private final Circle previewDot;
    private ImageView active;
    private Timeline fillAnimation;

    /**
     * Builds an icon view for the given resource (e.g. "param_finances.png").
     *
     * @param resourceName file name of the icon, located at the root of resources
     */
    public ParameterIconView(final String resourceName) {
        getStyleClass().add("parameter-icon");
        this.previewDot = createPreviewDot();
        loadIcon(resourceName);
        bindFillToClip();
        applyInitialClip();
    }

    private Circle createPreviewDot() {
        final Circle dot = new Circle(DOT_RADIUS, Color.WHITE);
        dot.setOpacity(0);
        return dot;
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

            this.active = buildImageView(image);

            setAlignment(previewDot, Pos.TOP_CENTER);
            previewDot.setTranslateY(DOT_OFFSET_Y);

            getChildren().addAll(dimmed, this.active, previewDot);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load parameter icon: " + resourceName, e);
        }
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
     * Marks this icon as "about to be affected" by the pending decision.
     * Shows a small white dot above the icon.
     *
     * @param affected true to show the dot, false to hide it
     */
    public void setAffected(final boolean affected) {
        previewDot.setOpacity(affected ? 1.0 : 0.0);
    }
}
