package net.thevpc.nmail;

public interface NScorableValue<T> {
    int getScore();
    T getValue();
}
