package com.meteordevelopments.duels.config;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.config.AbstractConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CommandsConfig extends AbstractConfiguration<DuelsPlugin> {

    private static final int DEFAULT_VERSION = 1;

    private final Map<CommandKey, CommandSettings> commands = new EnumMap<>(CommandKey.class);
    private int version;

    public CommandsConfig(final DuelsPlugin plugin) {
        super(plugin, "commands");
    }

    @Override
    protected void loadValues(final FileConfiguration configuration) throws Exception {
        version = configuration.getInt("config-version", DEFAULT_VERSION);
        for (final CommandKey key : CommandKey.values()) {
            commands.put(key, readSettings(configuration, key));
        }
    }

    public int getVersion() {
        return version;
    }

    public CommandSettings get(final CommandKey key) {
        return commands.getOrDefault(key, key.getDefaults());
    }

    private CommandSettings readSettings(final FileConfiguration configuration, final CommandKey key) {
        final String path = String.format("commands.%s", key.getPath());
        final String name = normalize(configuration.getString(path + ".name"), key.getDefaultName());
        final List<String> aliases = normalizeAliases(configuration.getStringList(path + ".aliases"), name, key.getDefaultAliases());
        return new CommandSettings(name, aliases);
    }

    private String normalize(final String value, final String fallback) {
        if (value == null) {
            return fallback;
        }

        final String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private List<String> normalizeAliases(final List<String> rawAliases, final String primary, final List<String> fallback) {
        final List<String> base = rawAliases == null || rawAliases.isEmpty() ? fallback : rawAliases;
        final LinkedHashSet<String> cleaned = new LinkedHashSet<>();

        for (final String alias : base) {
            if (alias == null) {
                continue;
            }

            final String normalized = alias.trim().toLowerCase(Locale.ROOT);
            if (normalized.isEmpty() || normalized.equals(primary)) {
                continue;
            }

            cleaned.add(normalized);
        }

        return Collections.unmodifiableList(new ArrayList<>(cleaned));
    }

    public enum CommandKey {
        DUEL("duel", List.of("1v1")),
        PARTY("party", List.of("p", "duelparty", "dp")),
        QUEUE("queue", List.of("q")),
        SPECTATE("spectate", List.of("spec")),
        DUELS("duels", List.of("ds")),
        KIT("kit", Collections.emptyList());

        private final String path;
        private final String defaultName;
        private final List<String> defaultAliases;

        CommandKey(final String defaultName, final List<String> defaultAliases) {
            this.path = defaultName;
            this.defaultName = defaultName;
            this.defaultAliases = defaultAliases;
        }

        public String getPath() {
            return path;
        }

        public String getDefaultName() {
            return defaultName;
        }

        public List<String> getDefaultAliases() {
            return defaultAliases;
        }

        public CommandSettings getDefaults() {
            return new CommandSettings(defaultName, defaultAliases);
        }
    }

    public static final class CommandSettings {
        private final String name;
        private final List<String> aliases;

        public CommandSettings(final String name, final List<String> aliases) {
            this.name = Objects.requireNonNull(name, "name");
            this.aliases = Collections.unmodifiableList(new ArrayList<>(aliases == null ? Collections.emptyList() : aliases));
        }

        public String getName() {
            return name;
        }

        public List<String> getAliases() {
            return aliases;
        }

        public String[] getAliasArray() {
            return aliases.toArray(new String[0]);
        }
    }
}
