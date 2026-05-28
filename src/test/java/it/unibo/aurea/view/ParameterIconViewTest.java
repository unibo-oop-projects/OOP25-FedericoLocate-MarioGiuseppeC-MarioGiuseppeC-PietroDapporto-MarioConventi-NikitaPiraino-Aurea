package it.unibo.aurea.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Unit tests for ParameterIconView visual logic.
 */
@ExtendWith(ApplicationExtension.class)
class ParameterIconViewTest {

    private static final int AFFECTED_DELTA = 15;
    private static final int SMALL_DELTA = 10;
    private static final int SCENE_SIZE = 100;

    private ParameterIconView iconView;

    @Start
    void start(final Stage stage) {
        iconView = new ParameterIconView("param_finances.png");
        stage.setScene(new Scene(iconView, SCENE_SIZE, SCENE_SIZE));
        stage.show();
    }

    @Test
    void testPreviewDotAppearsWhenAffected(final FxRobot robot) {
        boolean hasVisibleDot = iconView.getChildren().stream()
            .anyMatch(node -> node instanceof Circle && node.getOpacity() > 0);
        assertFalse(hasVisibleDot, "Dot should not be visible initially");

        robot.interact(() -> iconView.setAffected(true, AFFECTED_DELTA));

        hasVisibleDot = iconView.getChildren().stream()
            .anyMatch(node -> node instanceof Circle && node.getOpacity() == 1.0);
        assertTrue(hasVisibleDot, "Dot should become visible when affected is true");
    }

    @Test
    void testPreviewDotDisappearsWhenNotAffected(final FxRobot robot) {
        robot.interact(() -> iconView.setAffected(true, SMALL_DELTA));
        robot.interact(() -> iconView.setAffected(false, 0));

        final boolean hasVisibleDot = iconView.getChildren().stream()
            .anyMatch(node -> node instanceof Circle && node.getOpacity() > 0);
        assertFalse(hasVisibleDot, "Dot should become invisible when affected is false");
    }
}
