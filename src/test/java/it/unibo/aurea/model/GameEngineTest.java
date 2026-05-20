package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.Parameter;

/**
 * Testing class for the GameEngine implementation.
 */
class GameEngineTest {

    private GameEngine engine;

    /**
     * Set up a new GameEngine before each test.
     */
    @BeforeEach
    void setUp() {
        this.engine = new GameEngineImpl(GameConfigImpl.createStandard(it.unibo.aurea.model.api.Difficulty.EASY), new Deck());
    }

    /**
     * Tests if the game starts correctly in the RUNNING state.
     */
    @Test
    void testGameInitialization() {
        assertEquals(GameState.RUNNING, engine.getGameState(), "Game must start in RUNNING state");
        engine.start();
        assertEquals(GameState.RUNNING, engine.getGameState(), "Game must remain RUNNING after starting");
    }

    /**
     * Tests if the game reaches the LOST state when a parameter's level hits the limit.
     */
    @Test
    void testGameLostOnParameterDeath() {
        engine.start();
        assertEquals(GameState.RUNNING, engine.getGameState());

        // We simulate a loss by reducing the value to 0.
        // This checks if the Engine correctly detects the "alive" status of its parameters.
        final Parameter finances = engine.getParameters().get(0);

        // Decreasing level to 0 to set the death condition in ParameterImpl.
        finances.modify(-finances.getLevel());

        assertEquals(GameState.LOST, engine.getGameState(), "Game must be LOST when a parameter hits 0");
    }

    /**
     * Tests if the game reaches the WON state when the clock finishes.
     */
    @Test
    void testGameWonOnTimeFinished() {
        engine.start();
        assertEquals(GameState.RUNNING, engine.getGameState());

        // We simulate a win by advancing the clock until time is finished.
        // This validates the coordination between Engine and GameClock.
        while (!engine.getGameClock().isTimeFinished()) {
            engine.getGameClock().nextTurn();
        }

        assertEquals(GameState.WON, engine.getGameState(), "Game must be WON when time is finished");
    }

    /**
     * Tests that makeDecision properly applies effects, marks cards as used, and advances clock.
     */
    @Test
    void testMakeDecision() {
        engine.start();
        final it.unibo.aurea.model.api.Card cardBefore = engine.getCurrentCard();
        assertNotNull(cardBefore, "A card must be active at the start");

        final int turnBefore = engine.getGameClock().getCurrentTurn();
        final int semesterBefore = engine.getGameClock().getCurrentSemester();

        // Make decision
        engine.makeDecision(true);

        // Clock must have advanced
        final int turnAfter = engine.getGameClock().getCurrentTurn();
        assertTrue(turnAfter > turnBefore || engine.getGameClock().getCurrentSemester() > semesterBefore,
            "The game clock must advance after a decision");

        // The card played must be marked as used
        assertTrue(cardBefore.isUsed(), "The played card must be marked as used");
    }
}
