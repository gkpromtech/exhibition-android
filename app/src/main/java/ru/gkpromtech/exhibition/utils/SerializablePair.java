package ru.gkpromtech.exhibition.utils;

import java.io.Serializable;

public class SerializablePair<F, S> implements Serializable {
    public final F first;
    public final S second;

    public SerializablePair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }
}
