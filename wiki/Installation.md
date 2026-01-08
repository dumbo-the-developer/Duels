# Installation Guide

This guide will help you install and set up the Duels plugin on your Minecraft server.

## Requirements

### Server Requirements
- **Minecraft Server**: Spigot, Paper, or any Spigot-based fork (1.13+)
- **Java Version**: Java 8 or higher
- **Folia Support**: Yes, Duels is Folia-compatible

### Optional Dependencies
- **Vault** (Recommended) - For economy support and money betting
- **PlaceholderAPI** (Recommended) - For placeholder support in other plugins
- **WorldGuard** - For duelzone region restrictions

### Supported Plugin Integrations
- CombatTagPlus
- CombatLogX
- DeluxeCombat
- PvPManager
- Essentials
- mcMMO
- Factions / FactionsUUID
- MyPet
- BountyHunters
- NotBounties
- SimpleClans
- LeaderHeads
- MVdWPlaceholderAPI
- AxGraves
- Multiverse-Core
- My_Worlds
- MultiWorld

## Installation Steps

### 1. Download the Plugin

Download the latest version of Duels from:
- [SpigotMC Resource Page](https://www.spigotmc.org/resources/duelsoptimised.118881/)
- [Official Website](https://docs.meteordevelopments.com)

### 2. Install the Plugin

1. Stop your server if it's running
2. Place the `Duels.jar` file into your server's `plugins` folder
3. Start your server

The plugin will generate its configuration files on first launch:
```
plugins/Duels/
├── config.yml
├── commands.yml
├── lang.yml
├── lang_minimessages.yml
├── plugin.yml
├── arenas/
├── kits/
├── queues/
└── userdata/
```

### 3. Install Dependencies (Optional)

#### Installing Vault
1. Download Vault from [SpigotMC](https://www.spigotmc.org/resources/vault.34315/)
2. Place in your `plugins` folder
3. Install an economy plugin (e.g., EssentialsX)
4. Restart the server

#### Installing PlaceholderAPI
1. Download PlaceholderAPI from [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/)
2. Place in your `plugins` folder
3. Restart the server
4. The Duels expansion will automatically register

### 4. Initial Configuration

After the first start, check the console for:
```
[Duels] Enabling Duels vX.X.X
[Duels] Hooked into Vault!
[Duels] Hooked into PlaceholderAPI!
```

#### Set the Lobby Location

The lobby is where players teleport after a duel ends:

```
/duels setlobby
```

This command saves your current location as the lobby spawn point.

### 5. Create Your First Kit

Kits define the items players receive in a duel:

1. Fill your inventory with the items you want in the kit
2. Run the command:
   ```
   /duels savekit <kitname>
   ```

Example:
```
/duels savekit NoDebuff
```

### 6. Create Your First Arena

Arenas are where duels take place:

1. Go to the location for position 1
2. Run:
   ```
   /duels create <arenaname>
   /duels set <arenaname> 1
   ```
3. Go to the location for position 2
4. Run:
   ```
   /duels set <arenaname> 2
   ```
5. Enable the arena:
   ```
   /duels toggle <arenaname>
   ```

Example:
```
/duels create Arena1
/duels set Arena1 1
(move to second spawn)
/duels set Arena1 2
/duels toggle Arena1
```

### 7. Verify Installation

Test the plugin by challenging another player:

```
/duel <playername>
```

If everything is set up correctly, you'll see the duel request GUI.

## Configuration

After installation, you should configure the plugin to match your server's needs:

- **[config.yml](Configuration.md)** - Main configuration file
- **[commands.yml](Configuration.md#commands-configuration)** - Command aliases
- **[lang.yml](Configuration.md#language-configuration)** - Message translations

## Updating the Plugin

To update Duels to a newer version:

1. Stop your server
2. Replace the old `Duels.jar` in the `plugins` folder with the new version
3. Start your server
4. Check console for migration messages

**Note**: Always backup your `plugins/Duels/` folder before updating!

## Troubleshooting

### Plugin Not Loading

**Check**:
- Server version is 1.13 or higher
- No conflicting plugins
- Correct Java version

**Console Errors**: Check for red error messages in the console

### Commands Not Working

**Check**:
- You have the correct permissions
- There are no command conflicts in `commands.yml`

### Arenas Not Loading

**Check**:
- Arena positions are set (both position 1 and 2)
- Arena is enabled with `/duels toggle <arena>`
- World is loaded when Duels loads (add world manager plugins to softdepend if needed)

## Next Steps

Now that you've installed Duels, you can:

1. **Customize Configuration** - See [Configuration Guide](Configuration.md)
2. **Set Permissions** - See [Permissions Guide](Permissions.md)
3. **Create More Kits** - See [Kits Guide](Kits.md)
4. **Setup Queues** - See [Queue System](Queue-System.md)
5. **Configure GUIs** - See [GUI Configuration](GUI-Configuration.md)

## Support

If you need help with installation:

- **Discord**: [Support Server](https://discord.meteordevelopments.com)
- **SpigotMC**: [Resource Page](https://www.spigotmc.org/resources/duelsoptimised.118881/)

---

**Tip**: After installation, run `/duels help` to see all available commands!
