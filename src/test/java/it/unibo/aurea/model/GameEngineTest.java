package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Testing class for the GameEngine implementation.
 */
class GameEngineTest {

    private static final int LIFE_POINTS = 100;
    private GameEngine engine;

    /**
     * Set up a new GameEngine before each test.
     */
    @BeforeEach
    void setUp() {
        final GameConfig config = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
        this.engine = new GameEngineImpl(config, new GameClockImpl(config), new Deck());
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

        // We simulate a loss by applying an effect that drains a parameter to 0,
        // going through the public applyEffects() API (parameters are now read-only outside the engine).
        final ParameterType firstParam = engine.getParameters().get(0).getName();
        engine.applyEffects(List.of(new EffectImpl(firstParam, -LIFE_POINTS)));

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
