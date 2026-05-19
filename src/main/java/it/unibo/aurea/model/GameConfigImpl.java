package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.api.GameConfig;

/**
 * Here I've used a static factory pattern to handle the creation of many type of games.
 * For example in future could be interesting to create an apocalypse mode.
 */
public final class GameConfigImpl implements GameConfig {
    private static final int STANDARD_CARDS_PER_SEMESTER = 6;
    private static final int STANDARD_SEMESTERS_PER_GAME = 6;
    private static final int MINIMAL_NUMBER_SEMESTER = 2;
    private static final int MINIMAL_NUMBER_CARDS = 2;

    private final int semesters;
    private final int cardsPerSemester;
    private final Difficulty difficulty;

    private GameConfigImpl(final int cardsPerSemester, final int semestersPerGame, final Difficulty difficulty) {
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

    /**
     * @param difficulty the selected difficulty level
     * @return a standard configuration, so 3 years and 6 card per semester.
     */
    public static GameConfig createStandard(final Difficulty difficulty) {
        return new GameConfigImpl(STANDARD_CARDS_PER_SEMESTER, STANDARD_SEMESTERS_PER_GAME, difficulty);
    }

    /**
     * @param difficulty the selected difficulty level
     * @return a test configuration, so only 1 semester and 2 choices.
     */
    public static GameConfig createShort(final Difficulty difficulty) {
        return new GameConfigImpl(MINIMAL_NUMBER_CARDS, MINIMAL_NUMBER_SEMESTER, difficulty);
    }
}

