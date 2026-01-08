# Permissions

This page lists all permission nodes available in the Duels plugin.

## Permission Hierarchy

The plugin uses a hierarchical permission system:

```
duels.*
  └── duels.admin
       ├── duels.default
       │    ├── duels.duel
       │    ├── duels.stats
       │    ├── duels.toggle
       │    └── duels.top
       ├── duels.stats.others
       ├── duels.kits.*
       ├── duels.teleport.bypass
       ├── duels.spectate
       ├── duels.spectate.anonymously
       ├── duels.use.*
       │    ├── duels.use.kit-select
       │    ├── duels.use.arena-select
       │    ├── duels.use.own-inventory
       │    ├── duels.use.item-betting
       │    └── duels.use.money-betting
       └── duels.queue
```

---

## Core Permissions

### duels.*
- **Description**: Grants all Duels permissions
- **Default**: OP
- **Includes**: All permissions below

### duels.admin
- **Description**: Grants all administrative permissions
- **Default**: OP
- **Includes**: 
  - `duels.default`
  - `duels.stats.others`
  - `duels.kits.*`
  - `duels.teleport.bypass`
  - `duels.spectate`
  - `duels.spectate.anonymously`
  - `duels.use.*`
  - `duels.queue`

### duels.default
- **Description**: Grants all default player permissions
- **Default**: True
- **Includes**:
  - `duels.duel`
  - `duels.stats`
  - `duels.toggle`
  - `duels.top`

---

## Player Permissions

### duels.duel
- **Description**: Allows sending and accepting duel requests
- **Default**: True
- **Commands**:
  - `/duel <player>`
  - `/duel <player> <amount>`
  - `/duel accept <player>`
  - `/duel deny <player>`

### duels.stats
- **Description**: Allows viewing own statistics
- **Default**: True
- **Commands**:
  - `/duel stats`

### duels.stats.others
- **Description**: Allows viewing other players' statistics
- **Default**: OP
- **Commands**:
  - `/duel stats <player>`

### duels.toggle
- **Description**: Allows toggling duel requests on/off
- **Default**: True
- **Commands**:
  - `/duel toggle`

### duels.top
- **Description**: Allows viewing leaderboards
- **Default**: True
- **Commands**:
  - `/duel top [wins|losses|kit]`

### duels.queue
- **Description**: Allows using the queue system
- **Default**: OP (can be changed in config)
- **Commands**:
  - `/queue`
  - `/queue join`
  - `/queue leave`

### duels.spectate
- **Description**: Allows spectating duels
- **Default**: OP
- **Commands**:
  - `/spectate <player>`
  - `/spectate leave`

### duels.spectate.anonymously
- **Description**: Allows spectating without notifying players
- **Default**: OP
- **Effect**: Players won't see join/leave messages when you spectate

### duels.teleport.bypass
- **Description**: Bypass teleportation restrictions in duels
- **Default**: OP
- **Effect**: Allows teleporting to players in duels (prevents /tpa exploits)

---

## Feature Permissions

These permissions control access to specific features. They only take effect if the corresponding `use-permission` option is enabled in `config.yml`.

### duels.use.*
- **Description**: Grants all feature permissions
- **Default**: OP
- **Includes**:
  - `duels.use.kit-select`
  - `duels.use.arena-select`
  - `duels.use.own-inventory`
  - `duels.use.item-betting`
  - `duels.use.money-betting`

### duels.use.kit-select
- **Description**: Allows selecting kits in duel requests
- **Default**: True (unless `use-permission` is enabled)
- **Config**: `request.kit-selecting.use-permission`

### duels.use.arena-select
- **Description**: Allows selecting arenas in duel requests
- **Default**: True (unless `use-permission` is enabled)
- **Config**: `request.arena-selecting.use-permission`

### duels.use.own-inventory
- **Description**: Allows using own inventory in duels
- **Default**: True (unless `use-permission` is enabled)
- **Config**: `request.use-own-inventory.use-permission`

### duels.use.item-betting
- **Description**: Allows betting items in duels
- **Default**: True (unless `use-permission` is enabled)
- **Config**: `request.item-betting.use-permission`

### duels.use.money-betting
- **Description**: Allows betting money in duels
- **Default**: True (unless `use-permission` is enabled)
- **Config**: `request.money-betting.use-permission`

---

## Kit Permissions

### duels.kits.*
- **Description**: Grants access to all kits
- **Default**: OP

### duels.kits.<kitname>
- **Description**: Grants access to a specific kit
- **Default**: Depends on kit configuration
- **Example**: `duels.kits.nodebuff`

**Note**: Kit permissions are only required if you enable kit restrictions in the kit's options menu (`/duels options <kit>`).

---

## Admin Permissions

All administrative commands require the `duels.admin` permission.

### Arena Management
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels create <name>`
  - `/duels set <name> <1|2>`
  - `/duels delete <name>`
  - `/duels setarenaitem <name>`
  - `/duels info <name>`
  - `/duels toggle <name>`
  - `/duels tp <name> [1|2]`
  - `/duels list`

### Kit Management
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels savekit [-o] <name>`
  - `/duels loadkit <name>`
  - `/duels deletekit <name>`
  - `/duels setitem <name>`
  - `/duels options <name>`

### Queue Management
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels createqueue <kit> <bet>`
  - `/duels deletequeue <kit> <bet>`

### Sign Management
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels addsign <kit> <bet>`
  - `/duels delsign`

### User Management
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels setrating <player> <kit> <rating>`
  - `/duels resetrating <player> [kit]`
  - `/duels reset <player>`

### General Admin
- **Permission**: `duels.admin`
- **Commands**:
  - `/duels help [category]`
  - `/duels reload`
  - `/duels setlobby`
  - `/duels bind <kitname>`
  - `/duels playsound <sound>`

---

## Party Permissions

Party system uses the same permission as duels:

### duels.duel
- **Commands**:
  - `/party <player>` - Invite to party
  - `/party accept <player>`
  - `/party deny <player>`
  - `/party leave`
  - `/party disband`
  - `/party kick <player>`
  - `/party transfer <player>`
  - `/party list`
  - `/party toggle`
  - `/party friendlyfire`

---

## Kit Editor Permissions

### /kit edit <kitname>
- **Permission**: Depends on the kit
  - If kit restrictions are enabled: `duels.kits.<kitname>`
  - If kit restrictions are disabled: `duels.admin`

### /kit save <kitname>
- **Permission**: Same as `/kit edit`

---

## Permission Examples

### Basic Player Setup
Give players basic duel access:
```yaml
permissions:
  duels.default: true
```

This grants:
- `/duel` command
- `/duel stats`
- `/duel toggle`
- `/duel top`

### VIP Player Setup
Give VIP players queue access and spectating:
```yaml
permissions:
  duels.default: true
  duels.queue: true
  duels.spectate: true
```

### Moderator Setup
Give moderators additional management permissions:
```yaml
permissions:
  duels.admin: true
  duels.spectate.anonymously: true
```

### Kit-Specific Access
Give access to only specific kits:
```yaml
permissions:
  duels.default: true
  duels.kits.nodebuff: true
  duels.kits.uhc: true
```

**Note**: Requires kit restriction to be enabled in kit options.

### Feature Restrictions
Restrict money betting to VIP players:

**config.yml**:
```yaml
request:
  money-betting:
    enabled: true
    use-permission: true
```

**Permissions**:
```yaml
# Regular players
duels.default: true

# VIP players
duels.default: true
duels.use.money-betting: true
```

---

## LuckPerms Examples

### Grant Basic Permissions
```
/lp group default permission set duels.default true
```

### Grant Admin Permissions
```
/lp group admin permission set duels.admin true
```

### Grant Queue Access
```
/lp group vip permission set duels.queue true
```

### Grant Specific Kit
```
/lp group vip permission set duels.kits.nodebuff true
```

### Grant All Kits
```
/lp group vip permission set duels.kits.* true
```

---

## Permission Troubleshooting

### Players Can't Use Commands

**Check**:
1. Player has `duels.default` or `duels.duel` permission
2. No negative permissions (like `-duels.duel`)
3. Permission plugin is properly loaded

### Players Can't Select Kits/Arenas

**Check**:
1. `use-permission` is set to `false` in config.yml
2. If `use-permission` is `true`, player has the corresponding permission

### Players Can't Use Specific Kit

**Check**:
1. Kit restriction is enabled in kit options (`/duels options <kit>`)
2. Player has `duels.kits.<kitname>` or `duels.kits.*` permission

### Admin Commands Not Working

**Check**:
1. Player has `duels.admin` permission
2. No conflicting permissions
3. Restart server after changing permissions

---

## Default Permission Configuration

By default, the plugin is configured for public servers:

- **All Players**: Can duel, view stats, toggle requests
- **OP Players**: Full admin access, queue access, spectating

To change defaults, modify your permission plugin configuration.

---

## Related Documentation

- [Commands](Commands.md) - Complete command reference
- [Configuration](Configuration.md) - Permission-related config options
- [Kits](Kits.md) - Kit restriction setup
- [Queue System](Queue-System.md) - Queue permission setup
