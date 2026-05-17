package it.unibo.aurea.view.api;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;
import javafx.scene.layout.HBox;

/**
 * Will create a GUI report for the player. 
 */
public interface Report {
    /**
     * Displays the report Over screen.
     *
     * @param semesterLabel shows the actual semester.
     * @param levels shows the values of every semester.
     */
    void show(String semesterLabel, Map<ParameterType, Integer> levels);

    /**
     * Closes the report and continues the game. This method is made to don't receive the functional interface from PMD MAIN
     */
    void close();

    /**
     * @param title is the new Title.
     */
    void setTitle(String title);

    /**
     * @param subtitle  is the new subtitle.
     */
    void setSubtitle(String subtitle);

    /**
     * @param levels  are the new values of the parameters.
     */
    void setLevels(Map<ParameterType, Integer> levels);

    /**
     * @param actions  is the new buttonRow.
     */
    void setButtonAction(HBox actions);

    /**
     * @param background  is the new background, it's used when the backgorun changes(endgame).
     */
    void reveal(String background);
}
