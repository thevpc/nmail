package net.thevpc.nmail;

public interface NTracker {

    void setRowStatus(String rowId, Status status);

    Status getRowStatus(String rowId);

    enum Status {
        TODO,
        SUCCESS,
        ERROR,
    }
}
