package it.unibo.aurea.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.model.Deck;
import it.unibo.aurea.model.GameConfigImpl;
import it.unibo.aurea.model.GameEngineImpl;
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
            controller = new GameControllerImpl(fakeView, engine);
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

    /**
     * A fake class that acts as a "spy" on the Controller's actions.
     */
    private static final class FakeView implements GameView {

        private boolean isCardDisplayed;
        private boolean isParametersUpdated;

        // --- NEW METHOD REQUIRED BY THE INTERFACE ---
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

        @Override
        public void showVictory() { }

        @Override
        public void showDefeat() { }

        @Override
        public void showGameOver(final String reason) { }
    }
}
