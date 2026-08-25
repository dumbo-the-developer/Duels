package com.meteordevelopments.duels;

public final class Permissions {

    public static final String DUEL = "duels.duel";
    public static final String STATS = "duels.stats";
    public static final String STATS_OTHERS = STATS + ".others";
    public static final String TOGGLE = "duels.toggle";
    public static final String TOP = "duels.top";
    public static final String SPECTATE = "duels.spectate";
    public static final String SPEC_ANON = SPECTATE + ".anonymously";
    public static final String ADMIN = "duels.admin";
    public static final String TP_BYPASS = "duels.teleport.bypass";
    public static final String KIT = "duels.kits.%s";
    public static final String KIT_ALL = "duels.kits.*";
    public static final String KIT_EDIT = "duels.kiteditor";
    public static final String KIT_RESET_OTHERS = KIT_EDIT + ".reset.others";
    public static final String KIT_SELECTING = "duels.use.kit-select";
    public static final String OWN_INVENTORY = "duels.use.own-inventory";
    public static final String ARENA_SELECTING = "duels.use.arena-select";
    public static final String ITEM_BETTING = "duels.use.item-betting";
    public static final String MONEY_BETTING = "duels.use.money-betting";
    public static final String SETTING_ALL = "duels.use.*";
    public static final String QUEUE = "duels.queue";
    public static final String PARTY = "duels.party";
    public static final String PARTY_LIST_OTHERS = PARTY + ".list.others";
    public static final String PARTY_TOGGLE = PARTY + ".toggle";

    // Player-Created Custom Kits Permissions
    public static final String CUSTOMKITS_USE = "duels.customkits.use";
    public static final String CUSTOMKITS_CREATE = "duels.customkits.create";
    public static final String CUSTOMKITS_EDIT = "duels.customkits.edit";
    public static final String CUSTOMKITS_DELETE = "duels.customkits.delete";
    public static final String CUSTOMKITS_RENAME = "duels.customkits.rename";
    public static final String CUSTOMKITS_DUPLICATE = "duels.customkits.duplicate";
    public static final String CUSTOMKITS_LIMIT_PREFIX = "duels.customkits.limit.";
    public static final String CUSTOMKITS_LIMIT_UNLIMITED = "duels.customkits.limit.unlimited";
    public static final String CUSTOMKITS_ADMIN = "duels.customkits.admin";
    public static final String CUSTOMKITS_BYPASS_RESTRICTIONS = "duels.customkits.bypass.restrictions";

    // Replay Permissions
    public static final String REPLAY = "duels.replay";
    public static final String REPLAY_OTHERS = "duels.replay.others";
    public static final String REPLAY_ADMIN = "duels.replay.admin";

    private Permissions() {
    }
}
