# Commands

This page lists all commands available in the Duels plugin, including their syntax, permissions, and examples.

## Command Aliases

By default, Duels provides these command aliases (configurable in `commands.yml`):

- `/duel` - Aliases: `1v1`
- `/duels` - Aliases: `ds`
- `/party` - Aliases: `p`, `duelparty`, `dp`
- `/queue` - Aliases: `q`
- `/spectate` - Aliases: `spec`
- `/kit` - Aliases: `dkit`

---

## Player Commands

### /duel

Main command for sending and managing duel requests.

#### Syntax

```
/duel <player>
/duel <player> <amount>
/duel accept <player>
/duel deny <player>
/duel toggle
/duel stats [player]
/duel top [wins|losses|kit]
```

#### Subcommands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/duel <player>` | Send a duel request to a player | `duels.duel` | `/duel Steve` |
| `/duel <player> <amount>` | Send a duel request with money bet | `duels.duel` | `/duel Steve 100` |
| `/duel accept <player>` | Accept a duel request | `duels.duel` | `/duel accept Steve` |
| `/duel deny <player>` | Deny a duel request | `duels.duel` | `/duel deny Steve` |
| `/duel toggle` | Toggle receiving duel requests | `duels.toggle` | `/duel toggle` |
| `/duel stats [player]` | View duel statistics | `duels.stats` | `/duel stats Steve` |
| `/duel top [type]` | View top players leaderboard | `duels.top` | `/duel top wins` |

#### Examples

**Send a duel request:**
```
/duel Notch
```

**Send a duel request with $500 bet:**
```
/duel Notch 500
```

**Accept a duel from Steve:**
```
/duel accept Steve
```

**View your statistics:**
```
/duel stats
```

**View top winners:**
```
/duel top wins
```

**View top rating for a kit:**
```
/duel top NoDebuff
```

---

### /party

Commands for managing party duels.

#### Syntax

```
/party <player>
/party accept <player>
/party deny <player>
/party leave
/party disband
/party kick <player>
/party transfer <player>
/party list
/party toggle
/party friendlyfire
```

#### Subcommands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/party <player>` | Invite a player to your party | `duels.duel` | `/party Steve` |
| `/party accept <player>` | Accept a party invite | `duels.duel` | `/party accept Steve` |
| `/party deny <player>` | Deny a party invite | `duels.duel` | `/party deny Steve` |
| `/party leave` | Leave your current party | `duels.duel` | `/party leave` |
| `/party disband` | Disband your party (owner only) | `duels.duel` | `/party disband` |
| `/party kick <player>` | Kick a player from party (owner only) | `duels.duel` | `/party kick Steve` |
| `/party transfer <player>` | Transfer party ownership | `duels.duel` | `/party transfer Steve` |
| `/party list` | List all party members | `duels.duel` | `/party list` |
| `/party toggle` | Toggle party invites | `duels.duel` | `/party toggle` |
| `/party friendlyfire` | Toggle friendly fire in party | `duels.duel` | `/party friendlyfire` |

#### Examples

**Create a party:**
```
/party Notch
/party accept Notch
```

**Kick a member:**
```
/party kick Steve
```

**View party members:**
```
/party list
```

---

### /queue

Commands for joining and leaving the queue system.

#### Syntax

```
/queue
/queue join
/queue leave
```

#### Subcommands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/queue` | Open queue selection GUI | `duels.queue` | `/queue` |
| `/queue join` | Join a queue (GUI opens if multiple) | `duels.queue` | `/queue join` |
| `/queue leave` | Leave current queue | `duels.queue` | `/queue leave` |

#### Examples

**Open queue menu:**
```
/queue
```

**Leave queue:**
```
/queue leave
```

---

### /spectate

Commands for spectating duels.

#### Syntax

```
/spectate <player>
/spectate leave
```

#### Subcommands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/spectate <player>` | Spectate a player in a duel | `duels.spectate` | `/spectate Steve` |
| `/spectate leave` | Stop spectating | `duels.spectate` | `/spectate leave` |

#### Examples

**Spectate a player:**
```
/spectate Notch
```

**Stop spectating:**
```
/spectate leave
```

---

### /kit

Commands for editing kits (in-game kit editor).

#### Syntax

```
/kit edit <kitname>
/kit save <kitname>
```

#### Subcommands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/kit edit <kitname>` | Enter kit editing mode | Permission varies by kit | `/kit edit NoDebuff` |
| `/kit save <kitname>` | Save kit changes | Permission varies by kit | `/kit save NoDebuff` |

#### Examples

**Edit a kit:**
```
/kit edit NoDebuff
(Modify your inventory)
/kit save NoDebuff
```

---

## Admin Commands

### /duels

Main admin command for managing the plugin.

#### Syntax

```
/duels help [arena|kit|queue|sign|user|extra]
/duels reload
```

#### Help Subcommands

| Command | Description | Permission |
|---------|-------------|------------|
| `/duels help` | Show main help menu | `duels.admin` |
| `/duels help arena` | Show arena commands | `duels.admin` |
| `/duels help kit` | Show kit commands | `duels.admin` |
| `/duels help queue` | Show queue commands | `duels.admin` |
| `/duels help sign` | Show sign commands | `duels.admin` |
| `/duels help user` | Show user commands | `duels.admin` |
| `/duels help extra` | Show extra commands | `duels.admin` |

---

## Arena Commands

All arena commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels create <name>` | Create a new arena | `/duels create Arena1` |
| `/duels set <name> <1\|2>` | Set arena spawn position | `/duels set Arena1 1` |
| `/duels delete <name>` | Delete an arena | `/duels delete Arena1` |
| `/duels setarenaitem <name>` | Set arena display item | `/duels setarenaitem Arena1` |
| `/duels info <name>` | View arena information | `/duels info Arena1` |
| `/duels toggle <name>` | Enable/disable an arena | `/duels toggle Arena1` |
| `/duels tp <name> [1\|2]` | Teleport to arena position | `/duels tp Arena1 1` |
| `/duels list` | List all arenas | `/duels list` |

### Examples

**Create and configure an arena:**
```
/duels create PvPArena
/duels set PvPArena 1
(Move to second position)
/duels set PvPArena 2
/duels toggle PvPArena
```

**Set custom display item:**
```
(Hold the item you want)
/duels setarenaitem PvPArena
```

---

## Kit Commands

All kit commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels savekit [-o] <name>` | Save current inventory as kit | `/duels savekit NoDebuff` |
| `/duels loadkit <name>` | Load a kit to inventory | `/duels loadkit NoDebuff` |
| `/duels deletekit <name>` | Delete a kit | `/duels deletekit NoDebuff` |
| `/duels setitem <name>` | Set kit display item | `/duels setitem NoDebuff` |
| `/duels options <name>` | Open kit options GUI | `/duels options NoDebuff` |

### Examples

**Create a kit:**
```
(Fill your inventory with items)
/duels savekit UHC
```

**Overwrite existing kit:**
```
/duels savekit -o NoDebuff
```

**Set kit display item:**
```
(Hold a diamond sword)
/duels setitem NoDebuff
```

**Configure kit options:**
```
/duels options NoDebuff
```

Kit options GUI allows you to:
- Enable/disable the kit
- Set allowed arenas
- Configure hit delay
- Set combo effects

---

## Queue Commands

All queue commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels createqueue <kit> <bet>` | Create a queue | `/duels createqueue NoDebuff 0` |
| `/duels deletequeue <kit> <bet>` | Delete a queue | `/duels deletequeue NoDebuff 0` |

### Examples

**Create a ranked queue:**
```
/duels createqueue NoDebuff 0
```

**Create a queue with betting:**
```
/duels createqueue UHC 500
```

**Delete a queue:**
```
/duels deletequeue NoDebuff 0
```

---

## Sign Commands

All sign commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels addsign <kit> <bet>` | Create a queue sign | `/duels addsign NoDebuff 0` |
| `/duels delsign` | Delete a queue sign | `/duels delsign` |

### Examples

**Create a queue sign:**
```
1. Place a sign
2. Look at the sign
3. Run: /duels addsign NoDebuff 0
```

**Delete a queue sign:**
```
1. Look at the queue sign
2. Run: /duels delsign
```

Signs automatically update with:
- Queue name
- Players in queue
- Players in matches

---

## User Management Commands

All user commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels setrating <player> <kit> <rating>` | Set player's kit rating | `/duels setrating Steve NoDebuff 1600` |
| `/duels resetrating <player> [kit]` | Reset player's rating | `/duels resetrating Steve` |
| `/duels reset <player>` | Reset all player stats | `/duels reset Steve` |

### Examples

**Set a player's rating for a kit:**
```
/duels setrating Notch NoDebuff 1800
```

**Reset a player's rating for a specific kit:**
```
/duels resetrating Notch NoDebuff
```

**Reset all ratings for a player:**
```
/duels resetrating Notch
```

**Reset all statistics for a player:**
```
/duels reset Notch
```

---

## Extra Commands

All extra commands require `duels.admin` permission.

| Command | Description | Example |
|---------|-------------|---------|
| `/duels setlobby` | Set lobby spawn location | `/duels setlobby` |
| `/duels reload` | Reload configuration files | `/duels reload` |
| `/duels bind <kitname>` | Bind a kit to your held item | `/duels bind NoDebuff` |
| `/duels playsound <sound>` | Test a sound | `/duels playsound BLOCK_NOTE_PLING` |

### Examples

**Set the lobby:**
```
/duels setlobby
```

**Reload the plugin:**
```
/duels reload
```

**Bind a kit to an item:**
```
(Hold an item, e.g., diamond sword)
/duels bind NoDebuff
```

Now right-clicking with that item opens the duel menu with the kit pre-selected.

**Test sounds:**
```
/duels playsound ENTITY_PLAYER_LEVELUP
```

---

## Permission-Based Access

Some commands have additional permission requirements:

- **Kit Selection** - `duels.use.kit-select` (if `use-permission` is enabled)
- **Arena Selection** - `duels.use.arena-select` (if `use-permission` is enabled)
- **Own Inventory** - `duels.use.own-inventory` (if `use-permission` is enabled)
- **Item Betting** - `duels.use.item-betting` (if `use-permission` is enabled)
- **Money Betting** - `duels.use.money-betting` (if `use-permission` is enabled)
- **Specific Kits** - `duels.kits.<kitname>` (if kit restriction is enabled)
- **Stats of Others** - `duels.stats.others` (to view other players' stats)
- **Teleport Bypass** - `duels.teleport.bypass` (bypass duel teleport restriction)
- **Anonymous Spectating** - `duels.spectate.anonymously` (spectate without notification)

---

## Command Tips

1. **Tab Completion** - All commands support tab completion for player names and kit/arena names
2. **Case Insensitive** - Kit and arena names are case-insensitive
3. **Shortcuts** - Use command aliases to save typing
4. **Permissions** - Make sure players have the correct permissions
5. **Help Menu** - Use `/duels help` for in-game command reference

---

For more information about specific features:
- [Permissions](Permissions.md) - Complete permission nodes list
- [Configuration](Configuration.md) - Command customization
- [Kits](Kits.md) - Kit system details
- [Arenas](Arenas.md) - Arena setup guide
- [Queue System](Queue-System.md) - Queue system documentation
