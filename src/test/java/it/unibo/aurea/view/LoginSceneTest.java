package it.unibo.aurea.view;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import it.unibo.aurea.controller.api.PlayerInfo;
import it.unibo.aurea.model.api.Difficulty;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * TestFX integration tests for the LoginScene.
 * Verifies form validation and callback execution by interacting with
 * the scene graph directly, avoiding brittle physical-click simulation.
 */
@ExtendWith(ApplicationExtension.class)
class LoginSceneTest {

    private final AtomicReference<PlayerInfo> capturedInfo = new AtomicReference<>();

    @Start
    void start(final Stage stage) {
        capturedInfo.set(null);
        final LoginScene loginScene = new LoginScene(capturedInfo::set);
        loginScene.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Finds the root of the login scene among all showing windows,
     * identifying it by the presence of the login fields.
     *
     * @return the root node of the login scene
     */
    private Parent loginRoot() {
        return Window.getWindows().stream()
            .filter(Window::isShowing)
            .map(Window::getScene)
            .filter(s -> s != null && !s.getRoot().lookupAll(".login-field").isEmpty())
            .map(javafx.scene.Scene::getRoot)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Login scene not found"));
    }

    @Test
    void testSuccessfulLogin(final FxRobot robot) {
        robot.interact(() -> {
            final Parent root = loginRoot();
            final List<TextField> fields = root.lookupAll(".login-field").stream()
                .filter(n -> n instanceof TextField)
                .map(n -> (TextField) n)
                .toList();
            fields.get(0).setText("Federico");
            fields.get(1).setText("Engineering");
            ((Button) root.lookup(".login-submit")).fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        final PlayerInfo info = capturedInfo.get();
        assertNotNull(info, "The callback should have been executed");
        assertEquals("Federico", info.rectorName(), "Rector name should match input");
        assertEquals("Engineering", info.faculty(), "Faculty should match input");
        assertEquals(Difficulty.EASY, info.difficulty(), "Default difficulty should be EASY");
    }

    @Test
    void testEmptyFieldsShowError(final FxRobot robot) {
        robot.interact(() -> {
            final Parent root = loginRoot();
            ((Button) root.lookup(".login-submit")).fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertNull(capturedInfo.get(), "Callback should not fire with empty fields");

        final Label errorLabel = (Label) loginRoot().lookup(".login-error");
        assertNotNull(errorLabel, "Error label should exist");
        assertTrue(errorLabel.isVisible(), "Error label should become visible");
    }
}
