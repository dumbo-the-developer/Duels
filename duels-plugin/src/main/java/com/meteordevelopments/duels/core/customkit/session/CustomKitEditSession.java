package com.meteordevelopments.duels.core.customkit.session;

import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomKitEditSession {

    public enum SessionState {
        VIEWING_MENU,
        EDITING_LAYOUT,
        EDITING_ITEM,
        BROWSING_MATERIALS,
        EDITING_ENCHANTMENTS,
        EDITING_ATTRIBUTES,
        EDITING_TRIMS,
        EDITING_POTIONS,
        EDITING_LORE,
        AWAITING_INPUT
    }

    private final UUID playerUuid;
    private final UUID kitId;
    private final CustomKitImpl draftKit;
    private final CustomKitImpl originalKit;
    private final boolean isNew;

    private SessionState state = SessionState.EDITING_LAYOUT;
    private int activeSlot = -1;
    private boolean isArmorSlot = false;
    private boolean isOffHandSlot = false;
    private long lastModified;

    public CustomKitEditSession(final UUID playerUuid,
                                final CustomKitImpl originalKit,
                                final CustomKitImpl draftKit,
                                final boolean isNew) {
        this.playerUuid = playerUuid;
        this.kitId = draftKit.getUniqueId();
        this.originalKit = originalKit;
        this.draftKit = draftKit;
        this.isNew = isNew;
        this.lastModified = System.currentTimeMillis();
    }

    public void touch() {
        this.lastModified = System.currentTimeMillis();
    }
}
