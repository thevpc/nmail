package net.thevpc.nmail;

public interface SupportedValue<T> {
    int getSupportLevel();
    T getValue();
}
