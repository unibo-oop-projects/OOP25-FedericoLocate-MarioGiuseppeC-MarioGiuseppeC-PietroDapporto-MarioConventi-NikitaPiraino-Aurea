package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.api.GameConfig;

/**
 * Here I've used a static factory pattern to handle the creation of many type of games.
 * For example in future could be interesting to create an apocalypse mode.
 */
final class GameConfigImpl implements GameConfig {
    private static final int STANDARD_CARDS_PER_SEMESTER = 6;
    private static final int STANDARD_SEMESTERS_PER_GAME = 6;
    private static final int MINIMAL_NUMBER_SEMESTER = 2;
    private static final int MINIMAL_NUMBER_CARDS = 2;

    private final int semesters;
    private final int cardsPerSemester;
    private final Difficulty difficulty;

    GameConfigImpl(final int cardsPerSemester, final int semestersPerGame, final Difficulty difficulty) {
        this.cardsPerSemester = cardsPerSemester;
        this.semesters = semestersPerGame;
        this.difficulty = difficulty;
    }

    @Override
    public int getCardsPerSemester() {
        return cardsPerSemester;
    }

    @Override
    public int getSemestersPerGame() {
        return semesters;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }
}

