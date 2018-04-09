package me.realized._duels.utilities;

public interface Reloadable {

    void handleReload(ReloadType type);

    enum ReloadType {

        WEAK, STRONG
    }
}
