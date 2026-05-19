package it.unibo.aurea.model;

import java.util.Objects;

import it.unibo.aurea.model.api.GameClock;
import it.unibo.aurea.model.api.GameConfig;

/**
 * implmentation of the GameClock.
 */
public final class GameClockImpl implements GameClock {
    private final GameConfig gameConfiguration;
    //we start from 0 with the counter.
    private int currentTurn;
    private int currentSemester;
    private boolean timeFinished;

    /**
     * this is the constructor method of the class.
     * 
     * @param gameConfiguration sets some setting of the game, like number of cards, semesters...
     */
    public GameClockImpl(final GameConfig gameConfiguration) {
        this.gameConfiguration = Objects.requireNonNull(gameConfiguration);
    }

    @Override
    public void nextTurn() {
        if (timeFinished) {
            throw new IllegalStateException("Game already finished");
        }
        if (hasNextTurnInSemester()) {
            currentTurn++;
        } else if (hasNextSemester()) {
            currentSemester++;
            currentTurn = 0;
        } else {
            timeFinished = true;
        }
    }

    private boolean hasNextSemester() {
        return this.currentSemester + 1 < this.gameConfiguration.getSemestersPerGame();
    }

    private boolean hasNextTurnInSemester() {
        return this.currentTurn + 1 < this.gameConfiguration.getCardsPerSemester();
    }

    //GETTERS
    @Override
    public boolean isTimeFinished() {
        return timeFinished;
    }

    @Override
    public int getCurrentSemester() {
        return currentSemester;
    }

    @Override
    public int getCurrentTurn() {
        return currentTurn;
    }
}
