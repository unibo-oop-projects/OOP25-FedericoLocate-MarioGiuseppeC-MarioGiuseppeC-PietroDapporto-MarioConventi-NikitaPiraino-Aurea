package it.unibo.aurea.model;

import it.unibo.aurea.model.api.GameConfig;

/**
 * Here I've used a static factory pattern to handle the creation of many type of games.
 * For example in futurre could be intersting to create an apocalypse mode.
 */
public final class GameConfigImpl implements GameConfig {
    private static final int STANDARD_CARDS_PER_SEMESTER = 6;
    private static final int STANDARD_SEMESTERS_PER_GAME = 6;
    private static final int MINIMAL_NUMBER_SEMESTER = 2;
    private static final int MINIMAL_NUMBER_CARDS = 2;
    private final int semesters;
    private final int cardsPerSemester;

    private GameConfigImpl(final int cardsPerSemester, final int semestersPerGame) {
        this.cardsPerSemester = cardsPerSemester;
        this.semesters = semestersPerGame;
    }

    @Override
    public int getCardsPerSemester() {
        return cardsPerSemester;
    }

    @Override
    public int getSemestersPerGame() {
        return semesters;
    }

    /**
     * @return a standard configuration, so 3 years and 6 card per semester.
     */
    public static GameConfig createStandard() {
        return new GameConfigImpl(STANDARD_CARDS_PER_SEMESTER, STANDARD_SEMESTERS_PER_GAME);
    }

    /**
     * @return a test configuration, so only 1 semester and 2 choices. It's made to test the end of the game situation.
     */
    public static GameConfig createShort() {
        return new GameConfigImpl(MINIMAL_NUMBER_CARDS, MINIMAL_NUMBER_SEMESTER);
    }

    @Override
    public it.unibo.aurea.model.api.Difficulty getDifficulty() {
        // Easy by Default
        return it.unibo.aurea.model.api.Difficulty.EASY;
    }

}
