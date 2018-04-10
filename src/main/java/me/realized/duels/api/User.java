package me.realized.duels.api;

import java.util.UUID;

public interface User {

    UUID getUuid();

    String getName();

    void setWins(final int wins);

    void setLosses(final int losses);

    void setRequests(final boolean requests);
}
