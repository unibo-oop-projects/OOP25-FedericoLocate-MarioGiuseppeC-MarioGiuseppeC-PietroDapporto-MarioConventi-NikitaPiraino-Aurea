package it.unibo.aurea.model;

import java.util.ArrayList;
import java.util.List;

import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterObserver;
import it.unibo.aurea.model.api.ParameterType;

/**
 * {@inheritDoc}.
 */
public final class ParameterImpl implements Parameter {

    /** Start level constant. */
    public static final int START_LEVEL = 50;
    /** Min level constant. */
    public static final int MIN_LEVEL = 0;
    /** Max level constant. */
    public static final int MAX_LEVEL = 100;

    private final ParameterType name;
    private final List<ParameterObserver> observers;
    private int level;
    private boolean alive;

    /**
     * Constructor of a specific parameter.
     *
     * @param name the name of a {@code ParameterType}
     */
    public ParameterImpl(final ParameterType name) {
        this.name = name;
        this.level = START_LEVEL;
        this.alive = true;
        this.observers = new ArrayList<>();
    }

    @Override
    public void addObserver(final ParameterObserver observer) {
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
        this.level += delta;
        if (this.level >= MAX_LEVEL) {
            this.level = MAX_LEVEL;
            this.alive = false;
        } else if (this.level <= MIN_LEVEL) {
            this.level = MIN_LEVEL;
            this.alive = false;
        }

        this.notifyObservers();
    }

    @Override
    public String getDeathReason() {
        if (this.isAlive()) {
            return "Still alive!";
        }
        if (this.level >= MAX_LEVEL) {
            return this.name + " reached maximum capacity (100). The university lost control!";
        } else {
            return this.name + " dropped to zero. The university collapsed!";
        }
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
        for (final ParameterObserver observer : this.observers) {
            observer.onParameterChanged(this.name, this.level);
        }
    }

}
