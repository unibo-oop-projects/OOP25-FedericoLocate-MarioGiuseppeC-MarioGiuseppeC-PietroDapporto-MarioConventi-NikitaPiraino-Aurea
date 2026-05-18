package it.unibo.aurea.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.controller.api.PlayerInfo;
import it.unibo.aurea.model.Deck;
import it.unibo.aurea.model.GameConfigImpl;
import it.unibo.aurea.model.GameEngineImpl;
import it.unibo.aurea.model.ParameterImpl;
import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.GameView;

/**
 * Integration and robustness tests for the GameControllerImpl.
 * Uses a FakeView to test the MVC communication without launching JavaFX.
 */
class GameControllerImplTest {

    private GameController controller;
    private FakeView fakeView;

    /**
     * Sets up a fresh MVC environment before each test.
     */
    @BeforeEach
    public void setUp() {
        try {
            // Initialize the real Model
            final Deck deck = new Deck();
            final GameEngineImpl engine = new GameEngineImpl(GameConfigImpl.createStandard(), deck);

            // Initialize the fake View
            fakeView = new FakeView();

            // Create the Controller
            controller = new GameControllerImpl(fakeView, engine, new PlayerInfo("Test Rector", "Test Faculty"));
            // Connect the controller to the fake view (as per interface requirement)
            fakeView.setController(controller);

        } catch (final IllegalStateException e) {
            throw new IllegalStateException("Error during test initialization", e);
        }
    }

    @Test
    void testStartGameTriggersView() {
        controller.startGame();
        assertTrue(fakeView.isCardDisplayed(), "The View should have displayed the first card.");
    }

    @Test
    void testMakeDecisionUpdatesView() {
        controller.startGame();
        fakeView.setCardDisplayed(false); 
        controller.makeDecision(true);
        assertTrue(fakeView.isCardDisplayed(), "The View should have been updated after the decision.");
    }

    @Test
    void testObserverUpdatesParameters() {
        controller.startGame();
        fakeView.setParametersUpdated(false);
        controller.makeDecision(false);
        assertTrue(fakeView.isParametersUpdated(), "The View should have been notified via Observer.");
    }

    @Test
    void testRobustnessNoCrash() {
        controller.startGame();
        assertDoesNotThrow(() -> {
            controller.makeDecision(true);
            controller.makeDecision(false);
            controller.makeDecision(true);
        }, "The controller should never throw unexpected exceptions during decisions.");
    }

    @Test
    void testGetPlayerInfo() {
        final PlayerInfo info = controller.getPlayerInfo();
        assertNotNull(info, "getPlayerInfo() should never return null");
        assertEquals("Test Rector", info.rectorName(),
            "rectorName should match the value passed at construction");
        assertEquals("Test Faculty", info.faculty(),
            "faculty should match the value passed at construction");
    }

    @Test
    void testGetCurrentParametersLevels() {
        controller.startGame();
        final Map<ParameterType, Integer> levels = controller.getCurrentParametersLevels();
        assertNotNull(levels, "getCurrentParametersLevels() should not return null");
        assertEquals(4, levels.size(),
            "Should return levels for all 4 parameter types");
        for (final ParameterType type : ParameterType.values()) {
            assertTrue(levels.containsKey(type),
                "Levels map should contain key: " + type);
            assertTrue(levels.get(type) >= ParameterImpl.MIN_LEVEL
                && levels.get(type) <= ParameterImpl.MAX_LEVEL,
                "Level for " + type + " should be within valid range");
        }
    }

    @Test
    void testPreviewDecisionDeltasReturnsNonNull() {
        controller.startGame();
        final Map<ParameterType, Integer> deltas =
            controller.previewDecisionDeltas(true);
        assertNotNull(deltas,
            "previewDecisionDeltas() should not return null");
    }

    @Test
    void testPreviewDecisionDeltasPositiveValues() {
        controller.startGame();
        final Map<ParameterType, Integer> deltas =
            controller.previewDecisionDeltas(true);
        for (final Map.Entry<ParameterType, Integer> entry : deltas.entrySet()) {
            assertTrue(entry.getValue() >= 0,
                "All delta values should be absolute (non-negative)");
        }
    }

    @Test
    void testPreviewDecisionDeltasBothDirections() {
        controller.startGame();
        final Map<ParameterType, Integer> approval =
            controller.previewDecisionDeltas(true);
        final Map<ParameterType, Integer> refusal =
            controller.previewDecisionDeltas(false);
        assertNotNull(approval, "Approval deltas should not be null");
        assertNotNull(refusal, "Refusal deltas should not be null");
    }

    @Test
    void testPreviewDecisionNeverThrows() {
        assertDoesNotThrow(() -> controller.previewDecision(true),
            "previewDecision() should not throw before startGame()");
        assertNotNull(controller.previewDecision(true),
            "previewDecision() should never return null");
    }

    @Test
    void testParameterLevelsChangeAfterDecision() {
        controller.startGame();
        final Map<ParameterType, Integer> before =
            new java.util.EnumMap<>(controller.getCurrentParametersLevels());
        controller.makeDecision(true);
        final Map<ParameterType, Integer> after =
            controller.getCurrentParametersLevels();
        boolean anyChanged = false;
        for (final ParameterType type : ParameterType.values()) {
            if (!before.get(type).equals(after.get(type))) {
                anyChanged = true;
                break;
            }
        }
        assertTrue(anyChanged,
            "At least one parameter level should change after a decision");
    }

    /**
     * A fake class that acts as a "spy" on the Controller's actions.
     */
    private static final class FakeView implements GameView {

        private boolean isCardDisplayed;
        private boolean isParametersUpdated;

        // --- METHOD REQUIRED BY THE INTERFACE ---
        @Override
        public void setController(final GameController controller) {
        }

        public boolean isCardDisplayed() {
            return this.isCardDisplayed;
        }

        public void setCardDisplayed(final boolean status) {
            this.isCardDisplayed = status;
        }

        public boolean isParametersUpdated() {
            return this.isParametersUpdated;
        }

        public void setParametersUpdated(final boolean status) {
            this.isParametersUpdated = status;
        }

        @Override
        public void displayCard(final Card card) {
            this.isCardDisplayed = true;
        }

        @Override
        public void updateSingleParameter(final ParameterType type, final int newValue) {
            this.isParametersUpdated = true;
        }

        // --- NEW METHOD REQUIRED BY THE INTERFACE ---
        @Override
        public void updateTime(final int semester, final int turn) { }

        @Override
        public void showVictory() { }

        @Override
        public void showDefeat() { }

        @Override
        public void showGameOver(final String reason) { }
    }
}
