package it.unibo.aurea.model.api;

/**
 * Represents the four parameters of the game.
 */
public enum ParameterType {
    FINANCES("Finances"),
    STUDENTS("Students"),
    PROFESSORS("Professors"),
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
