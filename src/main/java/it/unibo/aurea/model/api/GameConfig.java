package it.unibo.aurea.model.api;

/**
 * contains the settings of the game.
 */
public interface GameConfig {
    /**
     * @return how many cards are presented for semester to the player
     */
    int getCardsPerSemester();

    /**
     * @return how many semester are played in a game.
     */
    int getSemestersPerGame();

    /**
     * @return the chosen game difficulty.
     */
    Difficulty getDifficulty();

    //HERE WE ALSO SHOULD ADD THE CARD STORAGE(like name of the structure or the content itself)
}
