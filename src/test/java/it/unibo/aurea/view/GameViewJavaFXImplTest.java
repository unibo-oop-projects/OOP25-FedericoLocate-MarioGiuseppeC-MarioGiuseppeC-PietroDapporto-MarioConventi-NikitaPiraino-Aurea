package it.unibo.aurea.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.controller.api.PlayerInfo;
import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.api.ParameterType;
import javafx.stage.Stage;

/**
 * Tests the main GameView implementation and its integration with the Controller.
 * Uses Mockito to supply a fake GameController.
 */
@ExtendWith(ApplicationExtension.class)
class GameViewJavaFXImplTest {

    private static final int TEST_LEVEL = 80;
    private static final int TEST_SEMESTER = 2;

    private GameViewJavaFXImpl gameView;

    @Start
    void start(final Stage stage) {
        gameView = new GameViewJavaFXImpl(() -> { });
    }

    @BeforeEach
    void setUp() {
        final GameController mockedController = mock(GameController.class);
        final PlayerInfo fakeInfo = new PlayerInfo("Magnificent Mock", "Testing", Difficulty.HARD);

        when(mockedController.getPlayerInfo()).thenReturn(fakeInfo);

        gameView.setController(mockedController);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testUpdateSingleParameterDoesNotCrash() {
        assertDoesNotThrow(() -> {
            gameView.updateSingleParameter(ParameterType.FINANCES, TEST_LEVEL);
            WaitForAsyncUtils.waitForFxEvents();
        }, "Updating a parameter should not throw any exception");
    }

    @Test
    void testUpdateTimeFormatting() {
        assertDoesNotThrow(() -> {
            gameView.updateTime(TEST_SEMESTER, 0);
            WaitForAsyncUtils.waitForFxEvents();
        }, "Updating time should be handled safely by the JavaFX thread");
    }

    @Test
    void testShowEndgameScreens() {
        assertDoesNotThrow(() -> {
            gameView.showVictory();
            WaitForAsyncUtils.waitForFxEvents();
        }, "showVictory should build and display the endgame overlay without errors");

        assertDoesNotThrow(() -> {
            gameView.showDefeat();
            WaitForAsyncUtils.waitForFxEvents();
        }, "showDefeat should build and display the endgame overlay without errors");
    }
}
