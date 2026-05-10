package it.unibo.aurea.view.api;

import java.util.Map;

import it.unibo.aurea.model.api.ParameterType;

public interface Report {
    /**
     * Displays the report Over screen.
     *
     * @param semesterLabel shows the actual semester.
     * @param levels shows the values of every semester.
     */
    void show(final String semesterLabel, final Map<ParameterType, Integer> levels);
}
