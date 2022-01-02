package net.thevpc.gomail;

public interface SupportedValue<T> {
    int getSupportLevel();
    T getValue();
}
