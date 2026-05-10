package it.unibo.aurea.view.api;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;

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
}
