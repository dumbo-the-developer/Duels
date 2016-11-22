package me.realized.duels.utilities;

public interface Reloadable {

    void handleReload(ReloadType type);

    enum ReloadType {

        WEAK, STRONG
    }
}
