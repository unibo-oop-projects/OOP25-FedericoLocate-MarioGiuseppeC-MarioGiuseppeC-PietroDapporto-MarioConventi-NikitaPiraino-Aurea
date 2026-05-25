package it.unibo.aurea.model.api;

/**
 * Mutable game parameter. Extends {@link ParameterView} with the ability to modify the level.
 * Only the Model layer should hold references of this type.
 */
public interface Parameter extends ParameterView {
    /**
     * Raises or decreases the level of this parameter.
     *
     * @param delta the amount to add/subtract based on the sign.
     */
    void modify(int delta);
}

