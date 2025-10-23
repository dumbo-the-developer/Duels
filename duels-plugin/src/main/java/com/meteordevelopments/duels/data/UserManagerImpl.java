package com.meteordevelopments.duels.data;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.match.party.PartyDuelMatch;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.*;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.event.user.UserCreateEvent;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.user.User;
import com.meteordevelopments.duels.api.user.UserManager;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserManagerImpl implements Loadable, Listener, UserManager {

    private static final Calendar GREGORIAN_CALENDAR = new GregorianCalendar();
    private static final String ADMIN_UPDATE_MESSAGE = "&9[Duels] &bDuels &fv%s &7is now available for download! Download at: &c%s";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File folder;
    private final Map<UUID, UserData> users = new ConcurrentHashMap<>();
    private final Map<String, UUID> names = new ConcurrentHashMap<>();
    private final Map<Kit, TopEntry> topRatings = new ConcurrentHashMap<>();
    private volatile int defaultRating;
    private volatile int matchesToDisplay;
    @Getter
    private volatile boolean loaded;
    @Getter
    private volatile TopEntry wins;
    @Getter
    private volatile TopEntry losses;
    @Getter
    private volatile TopEntry noKit;
    private ScheduledTask topTask;

    public UserManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.folder = new File(plugin.getDataFolder(), "users");

        if (!folder.exists()) {
            folder.mkdir();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        this.defaultRating = config.getDefaultRating();
        this.matchesToDisplay = config.getMatchesToDisplay();

        if (matchesToDisplay < 0) {
            matchesToDisplay = 0;
        }

        plugin.doAsync(() -> {
            final File[] files = folder.listFiles();

            if (files != null) {
                for (final File file : files) {
                    final String fileName = file.getName();

                    if (!fileName.endsWith(".json")) {
                        continue;
                    }

                    final String name = fileName.substring(0, fileName.length() - 5);

                    final UUID uuid = UUIDUtil.parseUUID(name);

                    if (uuid == null || users.containsKey(uuid)) {
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                        final UserData user = JsonUtil.getObjectMapper().readValue(reader, UserData.class);

                        if (user == null) {
                            Log.warn(this, "Could not load userdata from file: " + fileName);
                            continue;
                        }

                        user.folder = folder;
                        user.defaultRating = defaultRating;
                        user.matchesToDisplay = matchesToDisplay;
                        user.refreshMatches();
                        // Player might have logged in while reading the file
                        names.putIfAbsent(user.getName().toLowerCase(), uuid);
                        users.putIfAbsent(uuid, user);
                    } catch (IOException ex) {
                        Log.error(this, "Could not load userdata from file: " + fileName, ex);
                    }
                }
            }

            loaded = true;
        });

        this.topTask = plugin.doSyncRepeat(() -> {
            final Collection<? extends Kit> kits = plugin.getKitManager().getKits();

            plugin.doAsync(() -> {
                if (!loaded) {
                    return;
                }

                TopEntry top;

                if ((top = get(config.getTopUpdateInterval(), wins, User::getWins, config.getTopWinsType(), config.getTopWinsIdentifier())) != null) {
                    wins = top;
                }

                if ((top = get(config.getTopUpdateInterval(), losses, User::getLosses, config.getTopLossesType(), config.getTopLossesIdentifier())) != null) {
                    losses = top;
                }

                if ((top = get(config.getTopUpdateInterval(), noKit, User::getRating, config.getTopNoKitType(), config.getTopNoKitIdentifier())) != null) {
                    noKit = top;
                }

                topRatings.keySet().removeIf(kit -> !kits.contains(kit));

                for (final Kit kit : kits) {
                    final TopEntry entry = topRatings.get(kit);

                    if ((top = get(config.getTopUpdateInterval(), entry, user -> user.getRating(kit), config.getTopKitType().replace("%kit%", kit.getName()),
                            config.getTopKitIdentifier())) != null) {
                        topRatings.put(kit, top);
                    }
                }
            });
        }, 20L * 5, 20L);
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(topTask);
        loaded = false;
        saveUsers(Bukkit.getOnlinePlayers());
        users.clear();
        names.clear();
        topRatings.clear();
    }

    @Nullable
    @Override
    public UserData get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        final UUID uuid = names.get(name.toLowerCase());
        return uuid != null ? get(uuid) : null;
    }

    @Nullable
    @Override
    public UserData get(@NotNull final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return users.get(uuid);
    }

    @Nullable
    @Override
    public UserData get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player.getUniqueId());
    }

    @Nullable
    @Override
    public TopEntry getTopWins() {
        return wins;
    }

    @Nullable
    @Override
    public TopEntry getTopLosses() {
        return losses;
    }

    @Nullable
    @Override
    public TopEntry getTopRatings() {
        return noKit;
    }

    @Nullable
    @Override
    public TopEntry getTopRatings(@NotNull final Kit kit) {
        Objects.requireNonNull(kit, "kit");
        return topRatings.get(kit);
    }

    public String getNextUpdate(final long creation) {
        return DateUtil.format((creation + config.getTopUpdateInterval() - System.currentTimeMillis()) / 1000L);
    }

    private TopEntry get(final long interval, final TopEntry previous, final Function<User, Integer> function, final String type, final String identifier) {
        if (previous == null || System.currentTimeMillis() - previous.getCreation() >= interval) {
            return new TopEntry(type, identifier, subList(sorted(function)));
        }

        return null;
    }

    private List<TopData> subList(final List<TopData> list) {
        return Collections.unmodifiableList(Lists.newArrayList(list.size() > 10 ? list.subList(0, 10) : list));
    }

    private List<TopData> sorted(final Function<User, Integer> function) {
        return users.values().stream()
                .map(data -> new TopData(data.getUuid(), data.getName(), function.apply(data)))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private UserData tryLoad(final Player player) {
        final File file = new File(folder, player.getUniqueId() + ".json");

        if (!file.exists()) {
            final UserData user = new UserData(folder, defaultRating, matchesToDisplay, player);
            plugin.doSync(() -> Bukkit.getPluginManager().callEvent(new UserCreateEvent(user)));
            return user;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final UserData user = JsonUtil.getObjectMapper().readValue(reader, UserData.class);

            if (user == null) {
                return null;
            }

            user.folder = folder;
            user.defaultRating = defaultRating;
            user.matchesToDisplay = matchesToDisplay;
            user.refreshMatches();

            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
            }

            return user;
        } catch (IOException ex) {
            Log.error(this, "An error occured while loading userdata of " + player.getName() + "!", ex);
            return null;
        }
    }

    private void saveUsers(final Collection<? extends Player> players) {
        for (final Player player : players) {
            final UserData user = users.remove(player.getUniqueId());

            if (user != null) {
                user.trySave();
            }
        }
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.doSyncAfter(() -> {
            if (plugin.getUpdateManager() != null) {
                if (plugin.getUpdateManager().updateIsAvailable() && (player.isOp() || player.hasPermission(Permissions.ADMIN))) {
                    player.sendMessage(StringUtil.color(String.format(ADMIN_UPDATE_MESSAGE, plugin.getUpdateManager().getLatestVersion(), plugin.getDescription().getWebsite())));
                }
            }
        }, 5L);

        final UserData user = users.get(player.getUniqueId());

        if (user != null) {
            if (!player.getName().equals(user.getName())) {
                user.setName(player.getName());
                names.put(player.getName().toLowerCase(), player.getUniqueId());
            }

            return;
        }

        plugin.doAsync(() -> {
            final UserData data = tryLoad(player);

            if (data == null) {
                lang.sendMessage(player, "ERROR.data.load-failure");
                return;
            }

            names.put(player.getName().toLowerCase(), player.getUniqueId());
            users.put(player.getUniqueId(), data);
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final UserData user = users.remove(uuid);

        if (user != null) {
            plugin.doAsync(() -> {
                user.trySave();

                // Put data back after saving to prevent concurrency issues
                users.put(uuid, user);
            });
        }
    }

    public void handleMatchEnd(final DuelMatch match, final Set<Player> winners) {
        final Player winner = winners.iterator().next();
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        final String message;

        if (!(match instanceof PartyDuelMatch partyMatch)) {
            final long duration = System.currentTimeMillis() - match.getStart();
            final long time = GREGORIAN_CALENDAR.getTimeInMillis();
            final Player loser = match.getArena().getOpponent(winner);
            final double health = Math.ceil(winner.getHealth()) * 0.5;
            final MatchData matchData = new MatchData(winner.getName(), loser.getName(), kitName, time, duration, health);
            final UserData winnerData = get(winner);
            final UserData loserData = get(loser);

            if (winnerData != null && loserData != null) {
                winnerData.addWin();
                loserData.addLoss();
                winnerData.addMatch(matchData);
                loserData.addMatch(matchData);

                final KitImpl kit = match.getKit();
                int winnerRating = winnerData.getRatingUnsafe(kit);
                int loserRating = loserData.getRatingUnsafe(kit);
                int change = 0;

                if (config.isRatingEnabled() && !(!match.isFromQueue() && config.isRatingQueueOnly())) {
                    change = NumberUtil.getChange(config.getKFactor(), winnerRating, loserRating);
                    winnerData.setRating(kit, winnerRating = winnerRating + change);
                    loserData.setRating(kit, loserRating = loserRating - change);
                }

                message = lang.getMessage("DUEL.on-end.opponent-defeat",
                        "winner", winner.getName(),
                        "loser", loser.getName(),
                        "health", matchData.getHealth(),
                        "kit", kitName,
                        "arena", match.getArena().getName(),
                        "winner_rating", winnerRating,
                        "loser_rating", loserRating,
                        "change", change
                );
            } else {
                message = null;
            }
        } else {
            final Party winnerParty = partyMatch.getPlayerToParty().get(winner);
            final Party loserParty = match.getArena().getOpponent(winnerParty);
            message = lang.getMessage("DUEL.on-end.party-opponent-defeat",
                    "winners", StringUtil.join(partyMatch.getNames(winnerParty), ", "),
                    "losers", StringUtil.join(partyMatch.getNames(loserParty), ", "),
                    "kit", kitName,
                    "arena", match.getArena().getName()
            );
        }

        if (message == null) {
            return;
        }

        if (config.isArenaOnlyEndMessage()) {
            match.getArena().broadcast(message);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }
    }

    public void handleTeamMatchEnd(final DuelMatch match, final Set<Player> winners, final Set<Player> losers) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        
        // Update wins and losses for all players
        for (final Player winner : winners) {
            final UserData winnerData = get(winner);
            if (winnerData != null) {
                winnerData.addWin();
            }
        }
        
        for (final Player loser : losers) {
            final UserData loserData = get(loser);
            if (loserData != null) {
                loserData.addLoss();
            }
        }
        
        // Handle rating updates for team matches
        if (config.isRatingEnabled() && !(!match.isFromQueue() && config.isRatingQueueOnly())) {
            final KitImpl kit = match.getKit();
            
            // Calculate average rating for each team
            int winnerTeamRating = 0;
            int loserTeamRating = 0;
            int winnerCount = 0;
            int loserCount = 0;
            
            for (final Player winner : winners) {
                final UserData winnerData = get(winner);
                if (winnerData != null) {
                    winnerTeamRating += winnerData.getRatingUnsafe(kit);
                    winnerCount++;
                }
            }
            
            for (final Player loser : losers) {
                final UserData loserData = get(loser);
                if (loserData != null) {
                    loserTeamRating += loserData.getRatingUnsafe(kit);
                    loserCount++;
                }
            }
            
            if (winnerCount > 0 && loserCount > 0) {
                winnerTeamRating /= winnerCount;
                loserTeamRating /= loserCount;
                
                final int change = NumberUtil.getChange(config.getKFactor(), winnerTeamRating, loserTeamRating);
                
                // Apply rating changes to all players
                for (final Player winner : winners) {
                    final UserData winnerData = get(winner);
                    if (winnerData != null) {
                        final int currentRating = winnerData.getRatingUnsafe(kit);
                        winnerData.setRating(kit, currentRating + change);
                    }
                }
                
                for (final Player loser : losers) {
                    final UserData loserData = get(loser);
                    if (loserData != null) {
                        final int currentRating = loserData.getRatingUnsafe(kit);
                        loserData.setRating(kit, currentRating - change);
                    }
                }
            }
        }
        
        final String winnerNames = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
        final String loserNames = losers.stream().map(Player::getName).collect(Collectors.joining(", "));
        
        final String message = lang.getMessage("DUEL.on-end.team-opponent-defeat",
                "winners", winnerNames,
                "losers", loserNames,
                "kit", kitName,
                "arena", match.getArena().getName()
        );
        
        if (config.isArenaOnlyEndMessage()) {
            match.getArena().broadcast(message);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
        }
    }

}
