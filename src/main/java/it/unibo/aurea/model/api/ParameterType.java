package it.unibo.aurea.model.api;

/**
 * Represents the four parameters of the game.
 */
public enum ParameterType {
    /** Financial resources of the university. */
    FINANCES("Finances"),
    /** Student population and satisfaction. */
    STUDENTS("Students"),
    /** Academic staff quality and quantity. */
    PROFESSORS("Professors"),
    /** Public image and institutional prestige. */
    REPUTATION("Reputation");

    private final String displayName;

    ParameterType(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a human-readable display name for this parameter.
     *
     * @return the display name (e.g. "Finances", "Students")
     */
    public String getDisplayName() {
        return displayName;
    }
}
