package com.meteordevelopments.duels.api.folialib.impl;

import com.meteordevelopments.duels.api.folialib.FoliaLib;

@SuppressWarnings("unused")
public class LegacyPaperImplementation extends LegacySpigotImplementation {

    public LegacyPaperImplementation(FoliaLib foliaLib) {
        super(foliaLib);
    }

    // Don't need to override anything, since we're extending BukkitImplementation
}
