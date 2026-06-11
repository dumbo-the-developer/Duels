package com.meteordevelopments.duelsffa.arena;

public enum LeaveReason {
    COMMAND(true),
    REGEN(true),
    LEFT_ARENA(true),
    DISCONNECT(false);

    private final boolean teleport;

    LeaveReason(final boolean teleport) {
        this.teleport = teleport;
    }

    public boolean shouldTeleport() {
        return teleport;
    }
}
