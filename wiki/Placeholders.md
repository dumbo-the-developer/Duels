# Placeholders

This page documents all PlaceholderAPI placeholders available in the Duels plugin.

## Requirements

- **PlaceholderAPI** plugin must be installed
- The Duels expansion is automatically registered when both plugins are loaded

## Installation

1. Install PlaceholderAPI from [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/)
2. Restart your server
3. Duels will automatically hook into PlaceholderAPI
4. Use placeholders in any plugin that supports PlaceholderAPI

---

## Player Statistics Placeholders

### Basic Stats

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%duels_wins%` | Player's total wins | `15` |
| `%duels_losses%` | Player's total losses | `7` |
| `%duels_wl_ratio%` | Win/Loss ratio | `2.14` |
| `%duels_wlr%` | Win/Loss ratio (alternative) | `2.14` |
| `%duels_can_request%` | Whether player accepts duel requests | `true` / `false` |

### Rating Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%duels_rating_<kit>%` | Player's rating for specific kit | `1450` |
| `%duels_rating_-` | Player's overall rating (no kit) | `1400` |

**Examples**:
- `%duels_rating_nodebuff%` - NoDebuff kit rating
- `%duels_rating_uhc%` - UHC kit rating
- `%duels_rating_sumo%` - Sumo kit rating
- `%duels_rating_-` - Overall rating for own inventory duels

---

## Match Placeholders

These placeholders work when the player is in a match or spectating.

### Match Information

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%duels_match_duration%` | Current match duration | `02:34` |
| `%duels_match_kit%` | Kit used in the match | `NoDebuff` |
| `%duels_match_arena%` | Arena name | `Arena1` |
| `%duels_match_bet%` | Bet amount | `500` |
| `%duels_match_rating%` | Your current rating for the kit | `1450` |

### Opponent Information

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%duels_match_opponent%` | Opponent's name | `Notch` |
| `%duels_match_opponent_rating%` | Opponent's rating | `1520` |
| `%duels_match_opponent_health%` | Opponent's health (in hearts) | `8.5` |
| `%duels_match_opponent_ping%` | Opponent's ping | `45` |

---

## Queue Placeholders

### Players in Queue

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%duels_getplayersinqueue_<queue>%` | Number of players waiting in queue | `3` |
| `%duels_getplayersplayinginqueue_<queue>%` | Number of players currently playing from that queue | `12` |

**Using Queue Names**:
```
%duels_getplayersinqueue_ranked%
%duels_getplayersinqueue_casual%
```

**Using Kit and Bet (Legacy)**:
```
%duels_getplayersinqueue_<kit>_<bet>%
```

**Examples**:
- `%duels_getplayersinqueue_nodebuff_0%` - Players in NoDebuff queue with 0 bet
- `%duels_getplayersinqueue_uhc_500%` - Players in UHC queue with $500 bet
- `%duels_getplayersplayinginqueue_nodebuff_0%` - Players currently in matches from this queue

---

## MVdWPlaceholderAPI Support

Duels also supports MVdWPlaceholderAPI with these placeholders:

| Placeholder | Description |
|-------------|-------------|
| `{duels_wins}` | Player's total wins |
| `{duels_losses}` | Player's total losses |
| `{duels_can_request}` | Whether player accepts duel requests |

---

## Placeholder Usage Examples

### Scoreboard (FeatherBoard)

```yaml
board:
  title:
    text:
      - '<rainbow>Duels Stats'
  lines:
    wins:
      text:
        - '&7Wins: &a%duels_wins%'
    losses:
      text:
        - '&7Losses: &c%duels_losses%'
    ratio:
      text:
        - '&7W/L: &b%duels_wlr%'
    rating:
      text:
        - '&7Rating: &e%duels_rating_nodebuff%'
```

### Chat Format (EssentialsX)

```yaml
format: '&7[&aW:{duels_wins}&7/&cL:{duels_losses}&7] &r{DISPLAYNAME}&7: &r{MESSAGE}'
```

### TAB Header/Footer

```yaml
header:
  - '&6&lDuels Server'
  - '&7Rating: &e%duels_rating_nodebuff%'
footer:
  - '&7Wins: &a%duels_wins% &7| &7Losses: &c%duels_losses%'
  - '&7W/L Ratio: &b%duels_wlr%'
```

### Actionbar (During Match)

```yaml
actionbar: '&7Opponent: &c%duels_match_opponent% &7(&4%duels_match_opponent_health%❤&7) &7Duration: &e%duels_match_duration%'
```

### Hologram (HolographicDisplays)

```
&6&lTop Players
&7
&e#1 &f{player_1} &7- &a%duels_wins% wins
&e#2 &f{player_2} &7- &a%duels_wins% wins
&e#3 &f{player_3} &7- &a%duels_wins% wins
```

### MOTD (Server List)

```yaml
motd:
  - '&6&lDuels Server'
  - '&7Players in Queue: &e%duels_getplayersinqueue_nodebuff_0%'
```

### Queue Signs

```
&6&l[Queue]
&3{queue_kit}
&7Queued: &e{queued}
&7In Match: &a{in_match}
```

Signs automatically update with queue information when using `/duels addsign`.

---

## Advanced Usage

### Conditional Placeholders (with PlaceholderAPI)

Using **CheckItem** or **ConditionalPerms**:

```yaml
# Give permission based on rating
check:
  - '%duels_rating_nodebuff% >= 1600'
grant:
  - 'vip.rank'
```

### DeluxeMenus Example

```yaml
menu_items:
  stats:
    material: PAPER
    display_name: '&eYour Stats'
    lore:
      - '&7Wins: &a%duels_wins%'
      - '&7Losses: &c%duels_losses%'
      - '&7Ratio: &b%duels_wlr%'
      - ''
      - '&7NoDebuff Rating: &e%duels_rating_nodebuff%'
      - '&7UHC Rating: &e%duels_rating_uhc%'
```

### Animated Scoreboard (AnimatedScoreboard)

```yaml
scoreboard:
  lines:
    - '&7&m--------------------'
    - '&eWins: &a%duels_wins%'
    - '&eLosses: &c%duels_losses%'
    - '&eRatio: &b%duels_wlr%'
    - ''
    - '&6NoDebuff: &e%duels_rating_nodebuff%'
    - '&6UHC: &e%duels_rating_uhc%'
    - '&7&m--------------------'
```

---

## Format Configuration

### Duration Format

The match duration format can be configured in `config.yml`:

```yaml
duration-format: 'mm:ss'
```

**Options**:
- `mm:ss` - Minutes and seconds (e.g., `02:34`)
- `HH:mm:ss` - Hours, minutes, and seconds (e.g., `00:02:34`)
- `m:ss` - Minutes and seconds without leading zero (e.g., `2:34`)

### Rating Display

Default rating values are configured in `config.yml`:

```yaml
rating:
  enabled: true
  default-rating: 1400
```

### Placeholder Defaults

Default values for placeholders when data is not available:

```yaml
# config.yml
user-not-found: 'User not found'
not-in-match: 'none'
no-kit: 'none'
no-opponent: 'No opponent'
```

---

## Troubleshooting

### Placeholder Shows as Raw Text

**Example**: `%duels_wins%` instead of `15`

**Solutions**:
1. Install PlaceholderAPI
2. Restart the server
3. Check console for `[Duels] Hooked into PlaceholderAPI!`
4. Verify the plugin you're using supports PlaceholderAPI

### Placeholder Shows Default Value

**Example**: `User not found` instead of actual wins

**Solutions**:
1. Player must have logged in at least once
2. Player data must be loaded (check `/duel stats`)
3. Wait a few seconds after login for data to load

### Queue Placeholders Not Working

**Solutions**:
1. Verify queue exists (`/duels createqueue <kit> <bet>`)
2. Use correct queue name or kit_bet format
3. Check for typos in kit name
4. Queue name is case-insensitive

### Match Placeholders Show "none"

**Solutions**:
1. Player must be in an active match
2. Placeholder won't work in lobby
3. Use spectator placeholders if spectating

---

## Placeholder Performance

### Efficient Placeholders

These placeholders are cached and efficient:
- `%duels_wins%`
- `%duels_losses%`
- `%duels_wlr%`
- `%duels_rating_<kit>%`

### Real-Time Placeholders

These placeholders update in real-time:
- `%duels_match_*%` - All match placeholders
- `%duels_getplayersinqueue_*%` - Queue placeholders

**Recommendation**: Don't refresh match placeholders more than once per second in scoreboards.

---

## Creating Custom Placeholders

If you need custom placeholders, you can use the Duels API with PlaceholderAPI expansion:

```java
public class CustomDuelsPlaceholders extends PlaceholderExpansion {
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        Duels duels = (Duels) Bukkit.getPluginManager().getPlugin("Duels");
        User user = duels.getUserManager().get(player);
        
        if (identifier.equals("custom_stat")) {
            // Your custom logic here
            return "value";
        }
        
        return null;
    }
}
```

See [API Documentation](API-Documentation.md) for more information.

---

## Related Documentation

- [Statistics](Statistics.md) - Stats system explained
- [Rating System](Rating-System.md) - ELO rating details
- [Queue System](Queue-System.md) - Queue system setup
- [API Documentation](API-Documentation.md) - Developer API

---

## Plugin Compatibility

Duels placeholders work with:

- ✅ **FeatherBoard**
- ✅ **TAB**
- ✅ **DeluxeMenus**
- ✅ **HolographicDisplays**
- ✅ **AnimatedScoreboard**
- ✅ **EssentialsX Chat**
- ✅ **LuckPerms** (via PlaceholderAPI)
- ✅ **ConditionalPerms**
- ✅ **Any plugin that supports PlaceholderAPI**
