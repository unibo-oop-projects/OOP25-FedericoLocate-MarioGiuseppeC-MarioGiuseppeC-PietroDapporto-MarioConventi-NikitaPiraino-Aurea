package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        this.engine = new GameEngineImpl(GameConfigImpl.createStandard(), new Deck());
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
}
