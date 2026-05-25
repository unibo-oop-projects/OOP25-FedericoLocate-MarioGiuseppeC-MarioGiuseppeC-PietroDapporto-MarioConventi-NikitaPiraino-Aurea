package it.unibo.aurea.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterObserver;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Implementation of a game parameter.
 */
public final class ParameterImpl implements Parameter {

    /** Start level constant for all parameters. */
    public static final int START_LEVEL = 50;
    /** Min allowed level. */
    public static final int MIN_LEVEL = 0;
    /** Max allowed level. */
    public static final int MAX_LEVEL = 100;

    private final ParameterType name;
    private final List<ParameterObserver> observers;
    private int level;
    private boolean alive;

    /**
     * Constructor of a specific parameter with the default starting level (50).
     *
     * @param name the name of a {@code ParameterType}
     */
    public ParameterImpl(final ParameterType name) {
        this.name = Objects.requireNonNull(name, "ParameterType must not be null");
        this.level = START_LEVEL;
        this.alive = true;
        this.observers = new ArrayList<>();
    }

    @Override
    public void addObserver(final ParameterObserver observer) {
        Objects.requireNonNull(observer, "Observer must not be null");
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void modify(final int delta) {
        if (!this.alive) {
            return;
        }
        this.level = clamp(this.level + delta);
        if (this.level == MIN_LEVEL || this.level == MAX_LEVEL) {
            this.alive = false;
        }
        notifyObservers();
    }

    /**
     * Clamps {@code value} to the range [{@link #MIN_LEVEL}, {@link #MAX_LEVEL}].
     *
     * @param value the raw value to clamp
     * @return the clamped value
     */
    private static int clamp(final int value) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, value));
    }

    @Override
    public String getDeathReason() {
        if (this.isAlive()) {
            return "Still alive!";
        }
        if (this.level >= MAX_LEVEL) {
            return this.name.getDisplayName()
                + " reached maximum capacity (100). The university lost control!";
        }
        return this.name.getDisplayName()
            + " dropped to zero. The university collapsed!";
    }

    @Override
    public boolean isAlive() {
       return this.alive;
    }

    @Override
    public ParameterType getName() {
        return this.name;
    }

    /**
     * Notifies all registered observers about a change in the parameter's level.
     */
    private void notifyObservers() {
        List.copyOf(observers).forEach(o -> o.onParameterChanged(this.name, this.level));
    }
}
