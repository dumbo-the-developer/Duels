---
description: Install, configure, and use the Duels plugin.
---

# Home

Duels adds 1v1 (and optionally team) fights with queues, arenas, and rewards.

### Compatibility

* **Platform:** Spigot / Paper
* **Minecraft versions:** _Add your supported versions here_ (example: `1.20.1–1.20.4`)
* **Dependencies:** _List any required plugins_ (example: Vault)

### Features

* Challenge players with `/duel <player>`
* Queue-based matchmaking
* Arena support (multiple arenas)
* Customizable kits and rules
* Rewards on win (money, items, commands)
* Messages fully configurable

### Installation

1. Download the plugin `.jar`.
2. Drop it into `plugins/`.
3. Restart the server.
4. Edit the generated config files.
5. Run `/duels reload`.

{% hint style="info" %}
If your plugin ships with multiple config files (arenas/kits/messages), list them here.
{% endhint %}

### Quick start (minimal setup)

Do these three things first:

1. **Create at least one arena**
2. **Create at least one kit** (or enable “no-kit” duels)
3. **Give yourself admin permission** so you can test

#### Example config

{% code title="config.yml" %}
```yaml
# Example only. Replace with your real keys.
settings:
  allow-requests: true
  request-timeout-seconds: 30

queue:
  enabled: true

rewards:
  win-commands:
    - "eco give %player% 100"
```
{% endcode %}

### Next steps

* [Commands](duels/commands.md)
* [Permissions](duels/permissions.md)
* [Placeholders](duels/placeholders.md)
* [FAQ](duels/faq.md)
* [Developer API](duels/developer-api.md)

### Support

* Add your support link (Discord/GitHub Issues)
* Include your server version, plugin version, and latest logs
